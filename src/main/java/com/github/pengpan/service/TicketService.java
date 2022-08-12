package com.github.pengpan.service;

import com.github.pengpan.entity.Config;
import com.github.pengpan.entity.ScheduleInfo;

import java.util.List;

/**
 * @author pengpan
 */
public interface TicketService {

    List<String> getKeyList(Config config);

    List<ScheduleInfo> getTicket(Config config, List<String> keyList);
}
