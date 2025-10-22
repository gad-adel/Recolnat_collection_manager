package org.recolnat.collection.manager.service;

import org.recolnat.collection.manager.web.dto.DoiDTO;

public interface DoiService {

    /**
     * Récupère les informations d'un DOI en fonction de son identifiant.
     *
     * @param doi identifiant à rechercher
     * @return le DOI
     */
    DoiDTO getDoi(String doi);
}
