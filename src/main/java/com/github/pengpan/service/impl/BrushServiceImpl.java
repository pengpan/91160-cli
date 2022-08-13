package com.github.pengpan.service.impl;

import com.github.pengpan.entity.Config;
import com.github.pengpan.entity.ScheduleInfo;
import com.github.pengpan.enums.BrushChannelEnum;
import com.github.pengpan.service.BrushService;
import com.github.pengpan.service.TicketService;
import com.github.pengpan.util.Assert;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author pengpan
 */
@Service
public class BrushServiceImpl implements BrushService {

    private static final ThreadLocal<Integer> currIndex = ThreadLocal.withInitial(() -> 0);

    @Resource(name = "firstTicketServiceImpl")
    private TicketService firstTicketService;

    @Resource(name = "secondTicketServiceImpl")
    private TicketService secondTicketService;

    @Override
    public TicketService getTicketService(BrushChannelEnum brushChannel) {
        if (brushChannel == BrushChannelEnum.CHANNEL_1) {
            return firstTicketService;
        }
        if (brushChannel == BrushChannelEnum.CHANNEL_2) {
            return secondTicketService;
        }
        int ci = currIndex.get();
        if (ci == 0) {
            currIndex.set(1);
            return firstTicketService;
        }
        if (ci == 1) {
            currIndex.set(0);
            return secondTicketService;
        }
        return null;
    }

    @Override
    public List<ScheduleInfo> getTicket(Config config) {
        TicketService ticketService = getTicketService(config.getBrushChannel());
        Assert.notNull(ticketService, "[ticketService]不能为空");
        List<String> keyList = ticketService.getKeyList(config);
        return ticketService.getTicket(config, keyList);
    }
}
