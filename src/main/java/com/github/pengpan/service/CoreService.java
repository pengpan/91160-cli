package com.github.pengpan.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.dialect.PropsUtil;
import com.ejlchina.data.TypeRef;
import com.ejlchina.json.JSONKit;
import com.github.pengpan.client.MainClient;
import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.cookie.CookieStore;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.entity.*;
import com.github.pengpan.enums.DataTypeEnum;
import com.github.pengpan.util.Assert;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        String encryptedUsername = Base64.encode(rsa.encrypt(username, KeyType.PublicKey));
        String encryptedPassword = Base64.encode(rsa.encrypt(password, KeyType.PublicKey));

        Map<String, String> fields = MapUtil.newHashMap();
        fields.put("username", encryptedUsername);
        fields.put("password", encryptedPassword);
        fields.put("target", SystemConstant.DOMAIN);
        fields.put("error_num", "0");
        fields.put("token", getToken());

        Response<Void> loginResp = mainClient.doLogin(SystemConstant.LOGIN_URL, fields);
        if (!loginResp.raw().isRedirect()) {
            return false;
        }

        String redirectUrl = loginResp.headers().get("Location");
        Response<Void> redirectResp = mainClient.loginRedirect(redirectUrl);
        boolean loginSuccess = redirectResp.raw().isRedirect();
        if (loginSuccess) {
            AccountStore.store(username, password);
        }
        return loginSuccess;
    }

    private String getToken() {
        String html = mainClient.htmlPage(SystemConstant.LOGIN_URL);
        Document document = Jsoup.parse(html);
        Element tokens = document.getElementById("tokens");
        Assert.notNull(tokens, "token获取失败");
        return tokens.val();
    }

    public List<Map<String, Object>> getData(DataTypeEnum dataType) {
        Assert.notNull(dataType, "[dataType]不能为空");
        String cities = ResourceUtil.readUtf8Str(dataType.getPath());
        return JSONKit.toBean(new TypeRef<List<LinkedHashMap<String, Object>>>() {
        }.getType(), cities);
    }

    public List<Map<String, Object>> getUnit(String cityId) {
        Assert.notBlank(cityId, "[cityId]不能为空");
        return mainClient.getUnit(cityId);
    }

    public List<Map<String, Object>> getDept(String unitId) {
        Assert.notBlank(unitId, "[unitId]不能为空");
        return mainClient.getDept(unitId);
    }

    public List<Map<String, Object>> getDoctor(String unitId, String deptId) {
        BrushSchData data = dept(unitId, deptId, null);
        return Optional.ofNullable(data).map(BrushSchData::getDoc).orElseGet(ArrayList::new).stream()
                .map(JSONKit::toJson)
                .map(x -> JSONKit.<Map<String, Object>>toBean(new TypeRef<LinkedHashMap<String, Object>>() {
                }.getType(), x))
                .collect(Collectors.toList());
    }

    public BrushSchData dept(String unitId, String deptId, String brushStartDate) {
        Assert.notBlank(unitId, "[unitId]不能为空");
        Assert.notBlank(deptId, "[deptId]不能为空");
        String url = "https://gate.91160.com/guahao/v1/pc/sch/dep";
        String date = StrUtil.isBlank(brushStartDate) ? DateUtil.today() : brushStartDate;
        int page = 0;
        String userKey = CookieStore.accessHash();
        String result = mainClient.dept(url, unitId, deptId, date, page, userKey);
        BrushSch brushSch = Optional.ofNullable(result).filter(JSONUtil::isTypeJSONObject)
                .map(x -> JSONKit.<BrushSch>toBean(BrushSch.class, x)).orElse(null);
        if (brushSch == null || !Objects.equals(1, brushSch.getResult_code()) || !"200".equals(brushSch.getError_code())) {
            log.warn("获取数据失败: {}", result);
            return null;
        }
        return brushSch.getData();
    }

    public List<Map<String, Object>> getMember() {
        String url = "https://user.91160.com/member.html";
        String html = mainClient.htmlPage(url);
        Document document = Jsoup.parse(html);
        Element tbody = document.getElementById("mem_list");
        Assert.notNull(tbody, "就诊人为空");
        Elements trs = tbody.getElementsByTag("tr");
        List<Map<String, Object>> memberList = new ArrayList<>();
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

    public void brushTicketTask(Config config) {
        log.info("挂号开始");

        List<String> keyList = getJSONPathKeys(config);
        keyList.forEach(log::info);

        for (int i = 1; ; i++) {
            log.info("[{}]努力刷号中...", i);

            List<ScheduleInfo> schInfoList = getValidScheduleInfos(config, keyList);

            if (CollUtil.isEmpty(schInfoList)) {
                // 休眠
                ThreadUtil.sleep(config.getSleepTime(), TimeUnit.MILLISECONDS);
                continue;
            }

            log.info("刷到号了");

            // 判断登录是否有效
            CookieStore.getLoginCookieNotNull();

            // 获取有效的参数列表
            List<Register> formList = schInfoList.stream().parallel()
                    .flatMap(x -> buildForm(x, config).stream())
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

    private List<ScheduleInfo> getValidScheduleInfos(Config config, List<String> keyList) {
        BrushSchData ticketData = dept(config.getUnitId(), config.getDeptId(), config.getBrushStartDate());
        String sch = Optional.ofNullable(ticketData).map(BrushSchData::getSch).map(JSONKit::toJson).orElseGet(String::new);

        // 获取有效的schedule_id
        List<ScheduleInfo> schInfoList = keyList.stream().parallel()
                .map(x -> jsonPathEval(sch, x))
                .filter(Objects::nonNull)
                .map(JSONKit::toJson)
                .map(x -> JSONKit.<ScheduleInfo>toBean(ScheduleInfo.class, x))
                .filter(x -> x.getLeft_num() > 0 && !"0".equals(x.getSchedule_id()))
                .sorted(Comparator.comparing(ScheduleInfo::getLeft_num).reversed())
                .collect(Collectors.toList());

        schInfoList.forEach(x -> log.info(JSONKit.toJson(x)));
        return schInfoList;
    }

    private Object jsonPathEval(String sch, String jsonPath) {
        try {
            return JsonPath.read(sch, jsonPath);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> getJSONPathKeys(Config config) {
        LocalDate brushStartDate = StrUtil.isBlank(config.getBrushStartDate())
                ? LocalDate.now()
                : LocalDateTimeUtil.parseDate(config.getBrushStartDate(), DatePattern.NORM_DATE_PATTERN);
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate localDate = brushStartDate.plusDays(i);
            String k = String.valueOf(localDate.getDayOfWeek().getValue());
            String v = String.valueOf(i);
            map.put(k, v);
        }
        List<String> weeks = config.getWeeks().stream()
                .map(map::get).collect(Collectors.toList());

        List<String> keyList = new ArrayList<>();
        for (String day : config.getDays()) {
            for (String week : weeks) {
                String key = StrUtil.format("$.{}.{}.{}", config.getDoctorId(), day, week);
                keyList.add(key);
            }
        }
        return keyList;
    }

    private boolean doRegister(List<Register> formList) {
        boolean isMock = PropsUtil.getSystemProps().getBool("mock", false);
        if (isMock) {
            log.info("模拟挂号成功");
            return true;
        }
        if (CollUtil.isEmpty(formList)) {
            log.info("预约失败，号源无效");
            return false;
        }
        for (Register form : formList) {
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

            if (!submitResp.raw().isRedirect()) {
                continue;
            }

            String redirectUrl = submitResp.headers().get("Location");
            String html = mainClient.htmlPage(redirectUrl);
            // 判断结果
            if (StrUtil.contains(html, "预约成功")) {
                log.info("预约成功");
                return true;
            }
            log.info("预约失败");
        }
        return false;
    }

    private List<Register> buildForm(ScheduleInfo schInfo, Config config) {
        String html = mainClient.orderPage(config.getUnitId(), config.getDeptId(), schInfo.getSchedule_id());
        Document document = Jsoup.parse(html);

        Elements elmLis = Optional.of(document)
                .map(x -> x.getElementById("delts"))
                .map(x -> x.getElementsByTag("li")).orElseGet(Elements::new);

        List<String> times = elmLis.stream().map(Element::text).collect(Collectors.toList());
        log.info("schedule_id: {}, times: {}", schInfo.getSchedule_id(), JSONKit.toJson(times));

        List<String> detlidList = elmLis.stream()
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
                .map(x -> Register.builder()
                        .schData(sch_data)
                        .unitId(config.getUnitId())
                        .depId(config.getDeptId())
                        .doctorId(config.getDoctorId())
                        .schId(schInfo.getSchedule_id())
                        .memberId(config.getMemberId())
                        .accept("1")
                        .timeType(schInfo.getTime_type())
                        .detlid(x)
                        .detlidRealtime(detlid_realtime)
                        .levelCode(level_code)
                        .addressId("3317")
                        .address("Civic Center")
                        .build())
                .collect(Collectors.toList());
    }

    public Date serverDate() {
        Response<Void> response = mainClient.serverTime();
        String date = response.raw().header("date");
        if (StrUtil.isEmpty(date)) {
            return new Date();
        }
        return DateUtil.parse(date, DatePattern.HTTP_DATETIME_PATTERN, Locale.US).toJdkDate();
    }
}
