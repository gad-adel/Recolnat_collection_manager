package org.recolnat.collection.manager.common.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableAsync
@Slf4j
@RequiredArgsConstructor
public class ScheduledOperations {


    private final org.recolnat.collection.manager.service.SpecimenStatisticsService specimenStatisticsService;

    /**
     * for caching:
     * note: there is a proxy class generated that intercepts all requests and responds with the cached value, but 'internal' calls within the same class will not get the cached value.
     * Only external method calls coming in through the proxy are intercepted.
     * This means that self-invocation, in effect, a method within the target object calling another method of the target object, will not lead to an actual cache interception at runtime
     * even if the invoked method is marked with @Cacheable.
     */
    @Scheduled(fixedDelayString = "${caching.statistics.statisticsTTL}")
    public void updateCacheStatistique() {
        log.debug("load statistics cache");
        specimenStatisticsService.clearHomePageStatisticsTTL();
        specimenStatisticsService.getHomePageStatistics();
    }

}
