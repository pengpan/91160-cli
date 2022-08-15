package com.github.pengpan.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.ejlchina.json.JSONKit;
import com.github.pengpan.common.cookie.CookieStore;
import com.github.pengpan.entity.Config;
import com.github.pengpan.entity.DoctorSch;
import com.github.pengpan.entity.ScheduleInfo;
import com.github.pengpan.service.AbstractTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author pengpan
 */
@Slf4j
@Service
public class SecondTicketServiceImpl extends AbstractTicketService {

    @Cacheable(value = "KEY_LIST", key = "'second'")
    @Override
    public List<String> getKeyList(Config config) {
        LocalDate brushStartDate = StrUtil.isBlank(config.getBrushStartDate())
                ? LocalDate.now()
                : LocalDateTimeUtil.parseDate(config.getBrushStartDate(), DatePattern.NORM_DATE_PATTERN);
        Map<String, String> map = new LinkedHashMap<>();
        LongStream.range(0, 7).mapToObj(brushStartDate::plusDays).forEach(x -> {
            String week = String.valueOf(x.getDayOfWeek().getValue());
            String date = x.format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
            map.put(week, date);
        });
        List<String> weeks = config.getWeeks().stream()
                .map(map::get).collect(Collectors.toList());

        List<String> keyList = new ArrayList<>();
        for (String day : config.getDays()) {
            for (String week : weeks) {
                String key = StrUtil.format("$.{}.{}.{}",
                        StrUtil.format("{}_{}", config.getDeptId(), config.getDoctorId()),
                        StrUtil.format("{}_{}_{}", config.getDeptId(), config.getDoctorId(), day),
                        week);
                keyList.add(key);
            }
        }
        return keyList;
    }

    @Override
    public List<ScheduleInfo> getTicket(Config config, List<String> keyList) {
        String unitId = config.getUnitId();
        String deptId = config.getDeptId();
        String doctorId = config.getDoctorId();
        String brushStartDate = config.getBrushStartDate();

        String url = "https://gate.91160.com/guahao/v1/pc/sch/doctor";
        String date = StrUtil.isBlank(brushStartDate) ? DateUtil.today() : brushStartDate;
        String userKey = CookieStore.accessHash();
        int days = 6;
        DoctorSch doctorSch = mainClient.doctor(url, userKey, doctorId, doctorId, unitId, deptId, date, days);

        if (doctorSch == null || !Objects.equals(1, doctorSch.getCode())) {
            log.warn("获取数据失败: {}", JSONKit.toJson(doctorSch));
            return null;
        }

        Map<String, Object> ticketData = doctorSch.getSch();
        String sch = Optional.ofNullable(ticketData).map(JSONKit::toJson).orElseGet(String::new);

        return getScheduleInfos(sch, keyList);
    }
}
