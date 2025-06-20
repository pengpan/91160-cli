package com.github.pengpan.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.ejlchina.json.JSONKit;
import com.github.pengpan.client.MainClient;
import com.github.pengpan.entity.Config;
import com.github.pengpan.entity.ScheduleInfo;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pengpan
 */
@Slf4j
public abstract class AbstractTicketService implements TicketService {

    @Resource
    protected MainClient mainClient;

    protected List<ScheduleInfo> getScheduleInfos(String sch, List<String> keyList) {
        return keyList.stream().parallel()
                .map(x -> jsonPathEval(sch, x))
                .filter(Objects::nonNull)
                .map(JSONKit::toJson)
                .map(x -> JSONKit.<ScheduleInfo>toBean(ScheduleInfo.class, x))
                .filter(x -> x.getLeft_num() > 0 && !"0".equals(x.getSchedule_id()))
                .sorted(Comparator.comparing(ScheduleInfo::getLeft_num).reversed())
                .collect(Collectors.toList());
    }

    protected Object jsonPathEval(String sch, String jsonPath) {
        try {
            return JsonPath.read(sch, jsonPath);
        } catch (Exception ignored) {
            return null;
        }
    }

    protected long getMaxBrushDays(LocalDate brushStartDate,Config config) {
        String html = mainClient.deptPage(config.getDeptId());
        Document document = Jsoup.parse(html);

        String date = Optional.of(document)
                .map(x -> x.getElementById("minEntrance_height"))
                .map(x -> x.getElementsByTag("strong"))
                .filter(list -> list.size() > 1)
                .map(list -> list.get(1))
                .map(Element::text)
                .orElseGet(String::new);

        long maxBrushDays = 7;
        if (StrUtil.isNotBlank(date)) {
            LocalDateTime startTime = brushStartDate.atStartOfDay();
            LocalDateTime endTime = LocalDateTimeUtil.parse(StrUtil.removeAll(date, " "), DatePattern.CHINESE_DATE_PATTERN);
            maxBrushDays = LocalDateTimeUtil.between(startTime, endTime).toDays();
        }

        log.info("dept_id: {}, date: {}, maxBrushDay: {}", config.getDeptId(), date, maxBrushDays);
        return maxBrushDays;
    }
}
