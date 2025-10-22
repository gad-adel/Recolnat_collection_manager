package org.recolnat.collection.manager.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.StatisticsResult;
import org.recolnat.collection.manager.repository.jpa.InstitutionRepositoryJPA;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.repository.jpa.TaxonJPARepository;
import org.recolnat.collection.manager.service.SpecimenStatisticsService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * remark on cache:<br>
 * {@literal @}Cacheable: Triggers cache population.<br>
 * {@literal @}CacheEvict: Triggers cache eviction (deletion).<br>
 * {@literal @}CachePut: Updates the cache without interfering with the method execution.<br>
 * {@literal @}Caching: Regroups multiple cache operations to be applied on a method.<br>
 * {@literal @}CacheConfig: Shares some standard cache-related settings at the class level.<br>
 * <p>
 * remark on scheduled: (The {@literal @}EnableScheduling annotation is used to enable the scheduler for your application.)<br>
 *
 * @Scheduled(cron = "0 * 9 * * ?") Java Cron expressions are used to configure the instances of CronTrigger, a subclass of org.quartz.Trigger.
 * it's used to trigger the scheduler for a specific time period (or all times. Example :@Scheduled(cron ="1 * * * * *" )
 * For more information , see https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm <br>
 * @Scheduled(fixedRate = 1000) Fixed Rate scheduler is used to execute the tasks at the specific time. It does not wait for the completion of previous task. The values should be in milliseconds.<br>
 * @Scheduled(fixedDelay = 1000, initialDelay = 1000)  Fixed Delay scheduler is used to execute the tasks at a specific time. It should wait for the previous task completion. The values should be in milliseconds.<br>
 * <br>
 * fixedRateString and initialDelayString allow you to define variables in the Spring configuration file<br>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpecimenStatisticsServiceImpl implements SpecimenStatisticsService {

    private final SpecimenJPARepository specimenJPARepository;
    private final InstitutionRepositoryJPA institutionJPARepository;
    private final TaxonJPARepository taxonJPARepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable("statistics")
    @Transactional(readOnly = true)
    public StatisticsResult getHomePageStatistics() {

        Integer countSpecimens = specimenJPARepository.countDistinct();
        Integer countInstitutions = institutionJPARepository.countDistinct();
        Integer countTaxons = taxonJPARepository.countDistinct();

        return StatisticsResult.builder()
                .countSpecimen(countSpecimens)
                .countTaxon(countTaxons)
                .countInstitution(countInstitutions).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(value = "statistics", allEntries = true, beforeInvocation = true)
    public void clearHomePageStatisticsTTL() {
        log.debug("clear statistics cache");
    }

}
