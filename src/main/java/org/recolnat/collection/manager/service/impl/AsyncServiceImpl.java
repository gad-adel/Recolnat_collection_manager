package org.recolnat.collection.manager.service.impl;

import org.recolnat.collection.manager.service.AsyncService;
import org.recolnat.collection.manager.service.SpecimenStatisticsService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@EnableAsync
@Service
@RequiredArgsConstructor
public class AsyncServiceImpl implements AsyncService {
	
	private final SpecimenStatisticsService statisticsService;
	
	 @Async
	 public void updateCacheStatistique() {
		 statisticsService.clearHomePageStatisticsTTL();
		 statisticsService.getHomePageStatistics();
	 }

}
