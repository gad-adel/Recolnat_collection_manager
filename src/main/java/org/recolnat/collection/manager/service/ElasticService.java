package org.recolnat.collection.manager.service;

import org.recolnat.collection.manager.api.domain.Institution;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.SpecimenIndex;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


public interface ElasticService {

    /**
     * Ajout ou mise à jour complète dans elastic d'un specimen
     *
     * @param specimen   spécimen à créer ou mettre à jour
     * @param collection collection à affecter au spécimen
     */
    void addOrUpdateRefSpecimenElastic(Specimen specimen, CollectionJPA collection);

    /**
     * Ajout ou mise à jour complète dans elastic d'un specimen
     *
     * @param specimen    spécimen à créer ou mettre à jour
     * @param collection  collection à affecter au spécimen
     * @param institution institution à affecter au spécimen
     */
    void addOrUpdateRefSpecimenElastic(Specimen specimen, CollectionJPA collection, Institution institution);

    /**
     * Mise à jour partielle d' 'un specimen
     *
     * @param specimen propriété du spécimen à mettre à jour
     */
    void updatePartialSpecimenToRefElastic(SpecimenIndex specimen);

    /**
     * Suppression d'un specimen dans elastic
     *
     * @param uuid identifiant du spécimen à supprimer
     */
    void deleteSpecimenToRefElastic(String uuid);

    /**
     * test connection elastic
     * equivalent to 	elasticsearchClient.performRequest( new Request("HEAD", "/"));
     */
    boolean ping();

    /**
     * test si un index elastic existe
     * emploi de
     * elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(indiceName))).value();
     * ou de
     * Response response = client.performRequest(new Request("HEAD", "/" + index));
     * return response.getStatusLine().getStatusCode() == 200;```
     *
     * @param indexName nom de l'index à tester
     * @return si l'index existe
     */
    boolean verifyIndexExist(String indexName);

    /**
     * Vérifie l'existence d'un spécimen dans elastic
     *
     * @param uuid identifiant du spécimen à tester
     * @return si le spécimen existe dans l'index
     */
    boolean specimenExistInIndex(final String uuid);

    /**
     * Met à jour des spécimens en masse dans l'index
     *
     * @param index propriétés à mettre à jour
     * @param ids   identifiants des spécimens à modifier
     * @throws IOException
     */
    void bulkUpdate(SpecimenIndex index, List<String> ids) throws IOException;

    void updateSpecimenFromImport(UUID id, UUID institutionId);
}
