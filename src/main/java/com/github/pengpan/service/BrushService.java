package com.github.pengpan.service;

import com.github.pengpan.entity.Config;
import com.github.pengpan.entity.ScheduleInfo;

import java.util.List;

/**
 * @author pengpan
 */
public interface BrushService {

    TicketService getTicketService();

    List<ScheduleInfo> getTicket(Config config);
}
