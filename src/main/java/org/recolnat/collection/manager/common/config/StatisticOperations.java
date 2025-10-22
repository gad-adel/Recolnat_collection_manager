package org.recolnat.collection.manager.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.service.InstitutionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

@Profile(value = "!int & !test")
@Configuration
@EnableAsync
@Slf4j
@RequiredArgsConstructor
public class StatisticOperations {
    private final InstitutionService institutionService;

    @Scheduled(cron = "${statistic.cron}")
    public void runStatisticCron() {
        institutionService.refreshStatisticView();
    }
}
