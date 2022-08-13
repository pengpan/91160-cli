package com.github.pengpan.service;

import com.github.pengpan.entity.Config;
import com.github.pengpan.entity.ScheduleInfo;
import com.github.pengpan.enums.BrushChannelEnum;

import java.util.List;

/**
 * @author pengpan
 */
public interface BrushService {

    TicketService getTicketService(BrushChannelEnum brushChannel);

    List<ScheduleInfo> getTicket(Config config);
}
