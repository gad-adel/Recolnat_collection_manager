package org.recolnat.collection.manager.service;


import io.recolnat.model.CollectionUpdateDTO;
import org.recolnat.collection.manager.api.domain.CollectionCreate;

import java.util.UUID;

public interface CollectionIntegrationService {
    /**
     * Créer une collection.
     *
     * @param collection objet contenant les informations de la collection à créer
     * @return l'identifiant de la collection créée
     */
    UUID addCollection(CollectionCreate collection);

    /**
     * Met à jour une collection.
     *
     * @param collectionId        identifiant de la collection à mettre à jour
     * @param collectionUpdateDTO objet contenant les nouvelles informations
     * @return l'identifiant de la collection
     */
    UUID updateCollection(UUID collectionId, CollectionUpdateDTO collectionUpdateDTO);

    void deleteCollection(UUID collectionId);
}
