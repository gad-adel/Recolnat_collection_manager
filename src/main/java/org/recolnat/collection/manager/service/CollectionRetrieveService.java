package org.recolnat.collection.manager.service;

import io.recolnat.model.CollectionDescriptionDTO;
import io.recolnat.model.CollectionDetailDTO;
import io.recolnat.model.DomainSpecimenCountDTO;
import io.recolnat.model.UserCollectionDTO;
import org.recolnat.collection.manager.api.domain.Collection;
import org.recolnat.collection.manager.api.domain.CollectionDashboardProjection;
import org.recolnat.collection.manager.api.domain.CollectionProjection;
import org.recolnat.collection.manager.api.domain.NominativeCollectionDashboardProjection;
import org.recolnat.collection.manager.api.domain.Result;

import java.util.List;
import java.util.UUID;

public interface CollectionRetrieveService {

    List<Collection> retreiveCollectionsByInstitution(Integer institutionId, String language);

    Result<CollectionDashboardProjection> retreiveCollectionsByInstitution(UUID institutionId, int page, int size, String searchTerm, boolean isFr);

    Result<NominativeCollectionDashboardProjection> retreiveNominativeCollectionsByInstitution(UUID institutionId, int page, int size, String searchTerm);

    List<Collection> retreiveAllCollections();

    CollectionDetailDTO findCollectionDetailById(UUID collectionId);

    Collection findCollectionById(UUID collectionId);

    List<Collection> findCollectionsByIds(List<UUID> collectionIds);

    List<CollectionProjection> findAllOptions(UUID institutionId);

    List<UserCollectionDTO> findUserCollections();

    List<DomainSpecimenCountDTO> getDomainSpecimenCounts(UUID institutionId);

    List<CollectionDescriptionDTO> getCollectionsDescriptions(UUID institutionId, String lng);
}
