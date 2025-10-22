package org.recolnat.collection.manager.common.config;

import org.recolnat.collection.manager.common.exception.FailedToStartException;
import org.recolnat.collection.manager.service.ElasticService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 *cette verification ne s'applique que dans le cadre de deployement reel<br>
 */
@Configuration
@RequiredArgsConstructor
@Profile(value = "!local & !int & !test")
public class ValidatorIndex {
	
	private final ElasticService elasticService;
	
	@Value("${index.specimen}")
	String indexSpecimen;
	
	
	/**
	 * verification si elastic accessible et verification si l index elastic, qui est associé au traitement des datas, est bien present.<br>
	 *  Sinon, le client low level cree un index, automatiquement, lors de l'insertion d un premier  specimen, ce qui doit être évité<br>
	 *  la gestion de non creation d index, par le client, n'est configurable qu'au niveau global(sur role par exemple) dans Elastic<br>
	 *  <br>
	 *  Invoke with `0` to indicate no error or different code to indicate abnormal exit. <br>
	 *  <br>
	 *  ex: shutdownManager.initiateShutdown(0);
	 */

	@PostConstruct
	public void indexationElasticExist() {
		try {
			if(! elasticService.ping()) {
				 throw new FailedToStartException("no correct Elastic configuration");
			 }
			if(!elasticService.verifyIndexExist(indexSpecimen)) {
				 throw new FailedToStartException("Error cause by unidentified index");
			}
		}catch (FailedToStartException e) {
			throw e;
		}catch (Exception e) {
			 throw new FailedToStartException("pb on call elastic verification service");
		}
	}
	 

}
