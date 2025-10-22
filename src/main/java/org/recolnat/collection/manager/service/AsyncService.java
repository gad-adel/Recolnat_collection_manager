package org.recolnat.collection.manager.service;

import org.springframework.scheduling.annotation.Async;

public interface AsyncService {
	
	 @Async
	void updateCacheStatistique();

}
