package com.github.pengpan.service;

import com.ejlchina.json.JSONKit;
import com.github.pengpan.client.MainClient;
import com.github.pengpan.entity.ScheduleInfo;
import com.jayway.jsonpath.JsonPath;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author pengpan
 */
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
}
