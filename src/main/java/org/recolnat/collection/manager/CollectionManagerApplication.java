package org.recolnat.collection.manager;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = "org.recolnat.collection.manager.common.config")
public class CollectionManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollectionManagerApplication.class, args);
	}


	@Bean("cacheManager")
    CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("statistics");
    }


}
