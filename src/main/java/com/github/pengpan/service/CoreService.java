package com.github.pengpan.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.github.pengpan.client.MainClient;
import com.github.pengpan.common.cookie.CookieStore;
import com.github.pengpan.constant.SystemConstant;
import com.github.pengpan.enums.DataTypeEnum;
import com.github.pengpan.vo.RegisterForm;
import com.github.pengpan.vo.ScheduleInfo;
import com.github.pengpan.vo.SubmitBody;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author pengpan
 */
@Slf4j
@Service
public class CoreService {

    @Resource
    private MainClient mainClient;

    public boolean login(String username, String password) {
        Assert.notBlank(username, "用户名不能为空");
        Assert.notBlank(password, "密码不能为空");
        RSA rsa = SecureUtil.rsa(null, SystemConstant.PUBLIC_KEY);
        username = Base64.encode(rsa.encrypt(username, KeyType.PublicKey));
        password = Base64.encode(rsa.encrypt(password, KeyType.PublicKey));

        JSONObject fields = new JSONObject();
        fields.put("username", username);
        fields.put("password", password);
        fields.put("target", "https://www.91160.com");
        fields.put("error_num", "0");
        fields.put("token", getToken());

        Response<Void> loginResp = mainClient.doLogin("https://user.91160.com/login.html", fields);
        if (302 != loginResp.code()) {
            return false;
        }

        String redirectUrl = loginResp.headers().get("Location");
        Response<Void> redirectResp = mainClient.loginRedirect(redirectUrl);
        return 302 == redirectResp.code();
    }

    private String getToken() {
        String html = mainClient.htmlPage("https://user.91160.com/login.html");
        Document document = Jsoup.parse(html);
        Element tokens = document.getElementById("tokens");
        if (tokens == null) {
            throw new RuntimeException("token获取失败");
        }
        return tokens.val();
    }

    public List<Map<String, Object>> getData(DataTypeEnum dataType) {
        Assert.notNull(dataType);
        String cities = ResourceUtil.readUtf8Str(dataType.getPath());
        return JSON.parseArray(cities).stream()
                .map(JSON::toJSONString)
                .map(x -> JSON.<Map<String, Object>>parseObject(x, new TypeReference<LinkedHashMap<String, Object>>() {
                }.getType()))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getUnit(String cityId) {
        Assert.notBlank(cityId);
        return mainClient.getUnit(cityId);
    }

    public List<Map<String, Object>> getDept(String unitId) {
        Assert.notBlank(unitId);
        return mainClient.getDept(unitId);
    }

    public List<Map<String, Object>> getDoctor(String unitId, String deptId) {
        JSONObject data = dept(unitId, deptId);
        return Optional.ofNullable(data).map(x -> x.getJSONArray("doc")).orElseGet(JSONArray::new).stream()
                .map(JSON::toJSONString)
                .map(x -> JSON.<Map<String, Object>>parseObject(x, new TypeReference<LinkedHashMap<String, Object>>() {
                }.getType()))
                .collect(Collectors.toList());
    }

    public JSONObject dept(String unitId, String deptId) {
        Assert.notBlank(unitId);
        Assert.notBlank(deptId);
        String url = "https://gate.91160.com/guahao/v1/pc/sch/dep";
        String date = DateUtil.today();
        int page = 0;
        String userKey = CookieStore.accessHash();
        JSONObject result = mainClient.dept(url, unitId, deptId, date, page, userKey);
        String resultCode = result.getString("result_code");
        String errorCode = result.getString("error_code");
        if (!"1".equals(resultCode) || !"200".equals(errorCode)) {
            return new JSONObject();
        }
        return result.getJSONObject("data");
    }

    public JSONObject brushTicket(String docId) {
        String date = DateUtil.today();
        int days = 6;
        String ticket = mainClient.brushTicket(docId, date, days);
        return Optional.ofNullable(ticket)
                .filter(JSONUtil::isTypeJSONObject)
                .map(JSON::parseObject)
                .orElseGet(JSONObject::new);
    }

    public List<Map<String, Object>> getMember() {
        String url = "https://user.91160.com/member.html";
        String html = mainClient.htmlPage(url);
        Document document = Jsoup.parse(html);
        Element tbody = document.getElementById("mem_list");
        if (tbody == null) {
            throw new RuntimeException("就诊人为空");
        }
        List<Map<String, Object>> memberList = new ArrayList<>();
        Elements trs = tbody.getElementsByTag("tr");
        for (Element tr : trs) {
            String id = StrUtil.removePrefix(tr.id(), "mem");
            Elements tds = tr.getElementsByTag("td");
            Map<String, Object> member = new LinkedHashMap<>();
            member.put("id", id);
            member.put("name", tds.get(0).text());
            member.put("sex", tds.get(1).text());
            member.put("birth", tds.get(2).text());
            member.put("idCard", tds.get(3).text());
            member.put("mobile", tds.get(4).text());
            memberList.add(member);
        }
        return memberList;
    }

    public void brushTicketTask(SubmitBody body, int sleepTime) {
        log.info("挂号开始");

        LocalDate now = LocalDate.now();
        Map<String, String> map = new LinkedHashMap<>();
        LongStream.range(1, 8).mapToObj(now::plusDays).forEach(x -> {
            String week = String.valueOf(x.getDayOfWeek().getValue());
            String date = x.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            map.put(week, date);
        });
        List<String> weeks = body.getWeeks().stream()
                .map(map::get).collect(Collectors.toList());

        List<String> keyList = new ArrayList<>();
        for (String day : body.getDays()) {
            for (String week : weeks) {
                String key = StrUtil.format("$.sch.{}.{}.{}",
                        StrUtil.format("{}_{}", body.getDeptId(), body.getDoctorId()),
                        StrUtil.format("{}_{}_{}", body.getDeptId(), body.getDoctorId(), day),
                        week);
                keyList.add(key);
            }
        }

        keyList.forEach(System.out::println);

        for (; ; ) {
            log.info("努力刷号中...");

            JSONObject ticketData = brushTicket(body.getDoctorId());

            // 获取有效的schedule_id
            List<ScheduleInfo> schInfoList = keyList.stream().parallel()
                    .map(x -> JSONPath.eval(ticketData, x))
                    .filter(Objects::nonNull)
                    .map(JSON::toJSONString)
                    .map(x -> JSON.parseObject(x, ScheduleInfo.class))
                    .filter(x -> x.getNumber() != null && x.getNumber() > 0 && !"0".equals(x.getSchId()))
                    .sorted(Comparator.comparing(ScheduleInfo::getNumber).reversed())
                    .collect(Collectors.toList());

            schInfoList.forEach(System.out::println);

            if (CollUtil.isEmpty(schInfoList)) {
                // 休眠
                try {
                    TimeUnit.SECONDS.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            // 判断登录是否有效
            if (!CookieStore.isLogin()) {
                login(body.getUserName(), body.getPassword());
            }

            // 判断会员ID是否正确
            boolean exist = getMember().stream()
                    .map(x -> String.valueOf(x.get("id")))
                    .anyMatch(x -> StrUtil.equals(x, body.getMemberId()));
            if (!exist) {
                throw new RuntimeException("就诊人编码不正确，请检查");
            }

            // 获取有效的参数列表
            List<RegisterForm> formList = schInfoList.stream().parallel()
                    .flatMap(x -> buildForm(x, body).stream())
                    .collect(Collectors.toList());

            // 挂号
            boolean success = doRegister(formList);
            if (success) {
                log.info("挂号成功");
                break;
            }
        }

        log.info("挂号结束");
    }

    private boolean doRegister(List<RegisterForm> formList) {
        if (CollUtil.isEmpty(formList)) {
            return false;
        }
        for (RegisterForm form : formList) {
            Response<Void> submitResp = mainClient.doSubmit(
                    form.getSchData(),
                    form.getUnitId(),
                    form.getDepId(),
                    form.getDoctorId(),
                    form.getSchId(),
                    form.getMemberId(),
                    form.getAccept(),
                    form.getTimeType(),
                    form.getDetlid(),
                    form.getDetlidRealtime(),
                    form.getLevelCode(),
                    form.getAddressId(),
                    form.getAddress()
            );

            if (302 != submitResp.code()) {
                continue;
            }

            String redirectUrl = submitResp.headers().get("Location");
            String html = mainClient.htmlPage(redirectUrl);
            // 判断结果
            if (StrUtil.contains(html, "预约成功")) {
                return true;
            }
        }
        return false;
    }

    private List<RegisterForm> buildForm(ScheduleInfo schInfo, SubmitBody body) {
        String html = mainClient.orderPage(body.getUnitId(), body.getDeptId(), schInfo.getSchId());
        Document document = Jsoup.parse(html);

        List<String> detlidList = Optional.of(document)
                .map(x -> x.getElementById("delts"))
                .map(x -> x.getElementsByTag("li")).orElseGet(Elements::new)
                .stream()
                .map(x -> x.attr("val"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(detlidList)) {
            return CollUtil.newArrayList();
        }

        String sch_data = Optional.of(document)
                .map(x -> x.getElementsByAttributeValue("name", "sch_data"))
                .map(Elements::val)
                .orElseGet(String::new);
        if (StrUtil.isBlank(sch_data)) {
            return CollUtil.newArrayList();
        }

        String detlid_realtime = Optional.of(document)
                .map(x -> x.getElementById("detlid_realtime"))
                .map(Element::val)
                .orElseGet(String::new);
        if (StrUtil.isBlank(detlid_realtime)) {
            return CollUtil.newArrayList();
        }

        String level_code = Optional.of(document)
                .map(x -> x.getElementById("level_code"))
                .map(Element::val)
                .orElseGet(String::new);
        if (StrUtil.isBlank(level_code)) {
            return CollUtil.newArrayList();
        }

        return detlidList.stream()
                .map(x -> RegisterForm.builder()
                        .schData(sch_data)
                        .unitId(body.getUnitId())
                        .depId(body.getDeptId())
                        .doctorId(body.getDoctorId())
                        .schId(schInfo.getSchId())
                        .memberId(body.getMemberId())
                        .accept("1")
                        .timeType(schInfo.getTimeType())
                        .detlid(x)
                        .detlidRealtime(detlid_realtime)
                        .levelCode(level_code)
                        .addressId("3317")
                        .address("Civic Center")
                        .build())
                .collect(Collectors.toList());
    }

}
