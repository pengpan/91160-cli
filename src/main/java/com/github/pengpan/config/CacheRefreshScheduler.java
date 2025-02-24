package com.github.pengpan.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author pengpan
 */
@Slf4j
@Component
public class CacheRefreshScheduler {

    @Resource
    private CacheManager cacheManager;

    @Scheduled(cron = "0 5 */1 * * ?")
    public void refreshCache() {
        Cache cache = cacheManager.getCache("KEY_LIST");
        if (cache != null) {
            cache.clear();
            log.info("刷新缓存成功");
        }
    }
}
