package com.github.pengpan.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.ejlchina.json.JSONKit;
import com.github.pengpan.common.cookie.CookieStore;
import com.github.pengpan.entity.BrushSch;
import com.github.pengpan.entity.BrushSchData;
import com.github.pengpan.entity.Config;
import com.github.pengpan.entity.ScheduleInfo;
import com.github.pengpan.service.AbstractTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pengpan
 */
@Slf4j
@Service
public class FirstTicketServiceImpl extends AbstractTicketService {

    @Cacheable("FIRST_CHANNEL_KEY_LIST")
    @Override
    public List<String> getKeyList(Config config) {
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

    @Override
    public List<ScheduleInfo> getTicket(Config config, List<String> keyList) {
        String unitId = config.getUnitId();
        String deptId = config.getDeptId();
        String brushStartDate = config.getBrushStartDate();

        String url = "https://gate.91160.com/guahao/v1/pc/sch/dep";
        String date = StrUtil.isBlank(brushStartDate) ? DateUtil.today() : brushStartDate;
        int page = 0;
        String userKey = CookieStore.accessHash();
        BrushSch brushSch = mainClient.dept(url, unitId, deptId, date, page, userKey);

        if (brushSch == null || !Objects.equals(1, brushSch.getResult_code()) || !"200".equals(brushSch.getError_code())) {
            log.warn("获取数据失败: {}", JSONKit.toJson(brushSch));
            return null;
        }

        BrushSchData ticketData = brushSch.getData();
        String sch = Optional.ofNullable(ticketData).map(BrushSchData::getSch).map(JSONKit::toJson).orElseGet(String::new);

        return getScheduleInfos(sch, keyList);
    }
}
