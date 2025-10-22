package org.recolnat.collection.manager.service;


import io.recolnat.model.OperationTypeDTO;
import io.recolnat.model.PublicSpecimenDTO;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.SpecimenMerge;
import org.recolnat.collection.manager.api.domain.SpecimenPage;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;

import java.util.List;
import java.util.UUID;

public interface SpecimenIntegrationService {
    CollectionIdentifier add(Specimen specimen);

    Specimen getSpecimenById(UUID specimenId);

    CollectionIdentifier addAsDraft(Specimen specimen);

    CollectionIdentifier update(UUID specimenId, Specimen specimen);

    CollectionIdentifier updateAsDraft(UUID specimenId, Specimen specimen);

    CollectionIdentifier addAsReviewed(Specimen specimen);

    CollectionIdentifier updateAsReviewed(UUID specimenId, Specimen specimen);

    SpecimenPage getAllSpecimen(Integer pages, Integer size, String searchTerm, OperationTypeDTO state, Boolean currentDetermination, Boolean levelType,
                                String columnSort,
                                String typeSort, UUID institutionId, UUID collectionId, String collectionCode, String family, String genus,
                                String specificEpithet, String startDate, String endDate, String collector, String continent, String country,
                                String nominativeCollection, String storageName);

    List<UUID> updateMultipleSpecimen(List<UUID> specimenIds, SpecimenMerge specimen);

    List<CollectionIdentifier> bulkValidate(List<CollectionIdentifier> identifiers);

    void deleteSpecimen(UUID specimenId);

    SpecimenPage searchSpecimen(String searchWord, Integer pages, Integer size);

    SpecimenJPA saveSpecimenJPAAndUpdateMids(SpecimenJPA specimen);

    /**
     * Méthode employée pour l'affichage du detail d'un specimen, pour la partie public.
     * Attention emploi d'un MapStruct direct de JPA vers le DTO, pour une optimisation.
     *
     * @param specimenId identifiant du spécimen
     * @return le spécimen
     */
    PublicSpecimenDTO findDetailSpecimen(UUID specimenId);

    /**
     * test connection client low level Elastic (only for swagger)
     *
     * @return si elastic répond
     */
    boolean pingElastic();

    /**
     * test if index exist by client low level Elastic (only for swagger)
     *
     * @return si l'index existe
     */
    boolean indexElasticExist(String indexName);

    List<String> getCountriesByPrefix(String query, Integer size);

    List<String> getContinentsByPrefix(String query, Integer size);

    List<String> getRecordersByPrefix(String query, Integer size);

    List<String> getNominativeCollections(String query, Integer size);

    List<String> getNominativeCollectionsByInstitutionId(UUID institutionId);

    List<String> getStorageNames(String query, Integer size);

    boolean exists(UUID collectionId, String calatogNumber, UUID specimenId);

    /**
     * Indique s'il existe des spécimens à publier visible de l'utilisateur connecté
     *
     * @param institutionUuid identifiant de l'institution à filtrer (uniquement pour l'admin)
     * @return s'il existe des spécimens à publier visible de l'utilisateur connecté
     */
    Boolean hasSpecimentToPublish(UUID institutionUuid);
}
