package org.recolnat.collection.manager.service;

import org.recolnat.collection.manager.api.domain.StatisticsResult;


public interface SpecimenStatisticsService {

	/**
	 * retourne le count des institutions, specimens et taxons en base
	 * @return
	 */
    StatisticsResult getHomePageStatistics();
    
    /**
     * Ce traitement n'effectue que le vidage du cache<br>
     * Rem: Même avec beforeInvocation, l'éventuel appel, dans le corps de cette methode, de la methode getHomePageStatistics()
     * n' effectuera pas le chargement du cache. il est requis d'employer une autre methode pour le traitement du cron/fixedDelay.
     */
    void clearHomePageStatisticsTTL();
}
