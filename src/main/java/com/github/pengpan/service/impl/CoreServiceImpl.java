package com.github.pengpan.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.setting.dialect.PropsUtil;
import com.ejlchina.data.TypeRef;
import com.ejlchina.json.JSONKit;
import com.github.pengpan.client.MainClient;
import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.cookie.CookieStore;
import com.github.pengpan.entity.*;
import com.github.pengpan.enums.BrushChannelEnum;
import com.github.pengpan.enums.DataTypeEnum;
import com.github.pengpan.service.BrushService;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.util.Assert;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author pengpan
 */
@Slf4j
@Service
public class CoreServiceImpl implements CoreService {

    @Resource
    private MainClient mainClient;
    @Resource
    private BrushService brushService;

    @Override
    public List<Map<String, Object>> getData(DataTypeEnum dataType) {
        Assert.notNull(dataType, "[dataType]不能为空");
        String cities = ResourceUtil.readUtf8Str(dataType.getPath());
        return JSONKit.toBean(new TypeRef<List<LinkedHashMap<String, Object>>>() {
        }.getType(), cities);
    }

    @Override
    public List<Map<String, Object>> getUnit(String cityId) {
        Assert.notBlank(cityId, "[cityId]不能为空");
        return mainClient.getUnit(cityId);
    }

    @Override
    public List<Map<String, Object>> getLocalUnit(String cityId) {
        Assert.notBlank(cityId, "[cityId]不能为空");

        setCityCookie(cityId);
        Map<String, Object> fullUnit = mainClient.getLocalUnit();

        String cId = String.valueOf(fullUnit.get("city_id"));
        String content = String.valueOf(fullUnit.get("quikhospitals"));

        if (!StrUtil.equals(cId, cityId) || StrUtil.isBlank(content)) {
            return CollUtil.newArrayList();
        }

        Document document = Jsoup.parse(content);
        Elements optionList = document.getElementsByTag("option");

        List<Map<String, Object>> data = CollUtil.newArrayList();
        for (Element element : optionList) {
            String unitId = element.val();
            String unitName = element.text();
            if (!"0".equals(unitId)) {
                Map<String, Object> map = new HashMap<>();
                map.put("unit_id", unitId);
                map.put("unit_name", unitName);
                data.add(map);
            }
        }
        return data;
    }

    private void setCityCookie(String cityId) {
        String code = getCityPinYin(cityId);
        if (StrUtil.isBlank(code)) {
            return;
        }
        Cookie cookie = new Cookie.Builder().name("ip_city").value(code)
                .path("/").domain("91160.com").secure().httpOnly().build();
        CookieStore.remove(SystemConstant.HOST, "ip_city");
        CookieStore.put(SystemConstant.HOST, cookie);
    }

    private String getCityPinYin(String cityId) {
        List<Map<String, Object>> cities = getData(DataTypeEnum.CITIES);
        return cities.stream().parallel().filter(x -> StrUtil.equals(String.valueOf(x.get("cityId")), cityId)).findFirst()
                .map(x -> x.get("pinyin")).map(String::valueOf).orElseGet(String::new);
    }

    @Override
    public List<Map<String, Object>> getFullUnit(String cityId) {
        List<Map<String, Object>> localUnit = getLocalUnit(cityId);
        if (CollUtil.isNotEmpty(localUnit)) {
            return localUnit;
        }
        return getUnit(cityId);
    }

    @Override
    public List<Map<String, Object>> getDept(String unitId) {
        Assert.notBlank(unitId, "[unitId]不能为空");
        return mainClient.getDept(unitId).stream().flatMap(x -> {
            String child = Optional.ofNullable(x.get("childs")).map(JSONKit::toJson).orElseGet(String::new);
            return JSONKit.<List<LinkedHashMap<String, Object>>>toBean(new TypeRef<List<LinkedHashMap<String, Object>>>() {
            }.getType(), child).stream();
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDoctor(String unitId, String deptId) {
        BrushSchData data = dept(unitId, deptId, null);
        return Optional.ofNullable(data).map(BrushSchData::getDoc).orElseGet(ArrayList::new).stream()
                .map(JSONKit::toJson)
                .map(x -> JSONKit.<Map<String, Object>>toBean(new TypeRef<LinkedHashMap<String, Object>>() {
                }.getType(), x))
                .collect(Collectors.toList());
    }

    @Override
    public BrushSchData dept(String unitId, String deptId, String brushStartDate) {
        Assert.notBlank(unitId, "[unitId]不能为空");
        Assert.notBlank(deptId, "[deptId]不能为空");
        String url = "https://gate.91160.com/guahao/v1/pc/sch/dep";
        String date = StrUtil.isBlank(brushStartDate) ? DateUtil.today() : brushStartDate;
        int page = 0;
        String userKey = CookieStore.accessHash();
        BrushSch brushSch = mainClient.dept(url, unitId, deptId, date, page, userKey);

        if (brushSch == null || !Objects.equals(1, brushSch.getResult_code()) || !"200".equals(brushSch.getError_code())) {
            log.warn("获取数据失败: {}", JSONKit.toJson(brushSch));
            return null;
        }
        return brushSch.getData();
    }

    @Override
    public List<Map<String, Object>> getMember() {
        String url = "https://user.91160.com/member.html";
        String html = mainClient.htmlPage(url);
        Document document = Jsoup.parse(html);
        Element tbody = document.getElementById("mem_list");
        Assert.notNull(tbody, "就诊人为空，请先去家庭成员管理(https://user.91160.com/member.html)添加家庭成员并完成认证");
        Elements trs = tbody.getElementsByTag("tr");
        List<Map<String, Object>> memberList = new ArrayList<>();
        for (Element tr : trs) {
            String id = StrUtil.removePrefix(tr.id(), "mem");
            Elements tds = tr.getElementsByTag("td");
            Map<String, Object> member = new LinkedHashMap<>();
            member.put("id", id);

            boolean certified = tds.stream()
                    .map(Element::text)
                    .filter(x -> x.contains("认证"))
                    .findFirst()
                    .map("已认证"::equals)
                    .orElse(Boolean.FALSE);
            member.put("certified", certified);

            String name = tds.stream().findFirst()
                    .map(Element::text)
                    .map(x -> x.replace("默认", ""))
                    .map(StrUtil::trim).orElseGet(String::new);
            member.put("name", certified ? name : name + "（未认证）");
            memberList.add(member);
        }
        return memberList;
    }

    @Override
    public void brushTicketTask(Config config) {
        log.info("挂号开始");

        printBrushChannelInfo(config.getBrushChannel());

        for (int i = 1; ; i++) {
            log.info("[{}][{}]努力刷号中...", i, brushService.getCurrentBrushChannel().getName());

            List<ScheduleInfo> schInfoList = brushService.getTicket(config);

            if (CollUtil.isEmpty(schInfoList)) {
                // 休眠
                ThreadUtil.sleep(config.getSleepTime(), TimeUnit.MILLISECONDS);
                continue;
            }

            // 判断登录是否有效
            CookieStore.getLoginCookieNotNull();

            // 获取有效的参数列表
            List<Register> formList = schInfoList.stream().parallel()
                    .flatMap(x -> buildForm(x, config).stream())
                    .collect(Collectors.toList());

            // 依据配置的时间点过滤并排序
            if (CollUtil.isNotEmpty(formList) && CollUtil.isNotEmpty(config.getHours())) {
                formList = formList.stream()
                        .filter(x -> config.getHours().contains(x.getDetlName()))
                        .sorted(Comparator.comparing(Register::getDetlName))
                        .collect(Collectors.toList());
            }

            if (CollUtil.isEmpty(formList)) {
                // 休眠
                ThreadUtil.sleep(config.getSleepTime(), TimeUnit.MILLISECONDS);
                continue;
            }

            log.info("刷到号了");
            formList.forEach(x -> log.info(JSONKit.toJson(x)));

            // 判断登录是否有效
            CookieStore.getLoginCookieNotNull();

            // 挂号
            boolean success = doRegister(formList);
            if (success) {
                log.info("挂号成功");
                serverChanNotify(config);
                break;
            }
        }

        log.info("挂号结束");
    }

    private void serverChanNotify(Config config) {
        String sendKey = config.getSendKey();
        if (StrUtil.isBlank(sendKey)) {
            return;
        }
        String url = "https://sctapi.ftqq.com/" + config.getSendKey() + ".send";
        Map<String, Object> paramMap = MapUtil.<String, Object>builder()
                .put("title", "91160-cli")
                .put("desp", "挂号成功")
                .put("channel", "9")
                .build();
        String response = UnicodeUtil.toString(HttpUtil.post(url, paramMap));
        if (StrUtil.contains(response, "SUCCESS")) {
            log.info("通知成功");
        } else {
            log.info("通知失败 {}", response);
        }
    }

    private void printBrushChannelInfo(BrushChannelEnum brushChannel) {
        if (brushChannel == null) {
            log.info("当前刷号通道: 通道1+通道2");
        } else if (brushChannel == BrushChannelEnum.CHANNEL_1) {
            log.info("当前刷号通道: 通道1");
        } else if (brushChannel == BrushChannelEnum.CHANNEL_2) {
            log.info("当前刷号通道: 通道2");
        }
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

        int failCountMax = 3;
        String errorMsg = StrUtil.format("同一号源预约失败次数达到{}次，已终止程序！请检查号源是否有效！", failCountMax);
        Map<String, Integer> failCount = MapUtil.newHashMap();

        for (Register form : formList) {

            int count = failCount.getOrDefault(form.getSchId(), 0);
            Assert.isTrue(count < failCountMax, errorMsg);

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
                    form.getAddress(),
                    form.getHisMemId()
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
            log.info("预约失败:{}次 ({} {})", count + 1, form.getToDate(), form.getDetlName());

            failCount.put(form.getSchId(), ++count);
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

        List<Appointment> appointmentList = elmLis.stream()
                .map(x -> Appointment.builder().name(x.text()).value(x.attr("val")).build())
                .filter(x -> StrUtil.isNotBlank(x.getValue()))
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(appointmentList)) {
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

        return appointmentList.stream()
                .map(x -> Register.builder()
                        .schData(sch_data)
                        .unitId(config.getUnitId())
                        .depId(config.getDeptId())
                        .doctorId(schInfo.getDoctor_id())
                        .schId(schInfo.getSchedule_id())
                        .memberId(config.getMemberId())
                        .accept("1")
                        .timeType(schInfo.getTime_type())
                        .detlName(x.getName())
                        .detlid(x.getValue())
                        .detlidRealtime(detlid_realtime)
                        .levelCode(level_code)
                        .addressId("3317")
                        .address("Civic Center")
                        .toDate(schInfo.getTo_date())
                        .hisMemId(StrUtil.blankToDefault(config.getMedicalCard(), null))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Date serverDate() {
        Response<Void> response = mainClient.serverTime();
        String date = response.raw().header("date");
        if (StrUtil.isEmpty(date)) {
            return new Date();
        }
        return DateUtil.parse(date, DatePattern.HTTP_DATETIME_PATTERN, Locale.US).toJdkDate();
    }
}
