package org.recolnat.collection.manager.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.CollectionEvent;
import org.recolnat.collection.manager.api.domain.Location;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.service.MidsService;
import org.recolnat.collection.manager.web.dto.MidsDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
public class MidsServiceImpl implements MidsService {

    private static boolean hasScientificName(Specimen specimenToSave) {
        return !specimenToSave.getIdentifications().isEmpty() && specimenToSave.getIdentifications().stream()
                .anyMatch(i -> i.getTaxon().stream().anyMatch(t -> StringUtils.isNotBlank(t.getScientificName())));
    }

    @Override
    public MidsDTO processMids(Specimen specimen) {
        List<List<String>> missingInformationsMids0 = isMids0Validated(specimen);
        if (!missingInformationsMids0.isEmpty()) {
            return new MidsDTO(-1, missingInformationsMids0);
        }

        List<List<String>> missingInformationsMids1 = isMids1Validated(specimen);
        if (!missingInformationsMids1.isEmpty()) {
            return new MidsDTO(0, missingInformationsMids1);
        }

        List<List<String>> missingInformationsMids2 = isMids2Validated(specimen);
        if (!missingInformationsMids2.isEmpty()) {
            return new MidsDTO(1, missingInformationsMids2);
        }

        List<List<String>> missingInformationsMids3 = isMids3Validated(specimen);
        if (!missingInformationsMids3.isEmpty()) {
            return new MidsDTO(2, missingInformationsMids3);
        }

        // TODO à compléter une fois la règle établie
        //return new MidsDTO(3, Collections.emptyList());
        return new MidsDTO(2, Collections.emptyList());
    }

    /**
     * Vérifie la présence des informations suivante sur un spécimen :
     * <ul>
     *  <li>numéro d'inventaire</li>
     *  <li>identifiant d'institution</li>
     *  <li>identifiant de la collection</li>
     * </ul>
     *
     * @param specimen spécimen à vérifier
     * @return la liste des informations manquantes
     */
    public List<List<String>> isMids0Validated(Specimen specimen) {
        List<List<String>> missingInformations = new ArrayList<>();

        // PhysicalSpecimenID: Numéro d’inventaire (catalogNumber)
        boolean hasPhysicalSpecimenId = StringUtils.isNotBlank(specimen.getCatalogNumber());
        // Organization: Code d’institution (institutionId)
        boolean hasOrganization = StringUtils.isNotBlank(specimen.getInstitutionId());
        boolean hasCollection = specimen.getCollectionId() != null;

        if (!hasPhysicalSpecimenId) {
            missingInformations.add(Collections.singletonList("catalogNumber"));
        }
        if (!hasOrganization) {
            missingInformations.add(Collections.singletonList("institutionName"));
        }
        if (!hasCollection) {
            missingInformations.add(Collections.singletonList("collectionName"));
        }

        return missingInformations;
    }

    /**
     * Vérifie la présence des informations suivante sur un spécimen :
     * <ul>
     *  <li>nom scientifique</li>
     *  <li>identifiant d'un taxon (toujours vrai si nom scientifique)</li>
     *  <li>spécimen préservé (champ "basis_of_record sur specimen") (toujours vrai sur Recolnat)</li>
     *  <li>licence CCBY4.0 (pas présent sur Recolnat mais toujours vrai)</li>
     *  <li>date de modification (toujours valorisée)</li>
     * </ul>
     *
     * @param specimen spécimen à vérifier
     * @return la liste des informations manquantes
     */
    public List<List<String>> isMids1Validated(Specimen specimen) {
        List<List<String>> missingInformations = new ArrayList<>();
        boolean hasScientificName = hasScientificName(specimen);

        if (!hasScientificName) {
            missingInformations.add(Collections.singletonList("scientificName"));
        }
        return missingInformations;
    }

    /**
     * Vérifie la présence des informations suivante sur un spécimen :
     * <ul>
     *  <li>QualitativeLocation (au moins un champ renseigné)</li>
     *  <li>QuantitativeLocation : latitude ET longitude</li>
     *  <li>collecteur</li>
     *  <li>date de collecte</li>
     *  <li>numéro de collecte</li>
     *  <li>présence d'une image</li>
     * </ul>
     *
     * @param specimen spécimen à vérifier
     * @return la liste des informations manquantes
     */
    public List<List<String>> isMids2Validated(Specimen specimen) {
        List<List<String>> missingInformations = new ArrayList<>();

        CollectionEvent event = specimen.getCollectionEvent();
        if (event == null) {
            return List.of(List.of("all"));
        }
        Location location = event.getLocation();

        boolean hasQualitativeLocation = Stream.of(
                event.getVerbatimLocality(),
                location != null ? location.getLocality() : null,
                location != null ? location.getCounty() : null,
                location != null ? location.getMunicipality() : null,
                location != null ? location.getIsland() : null,
                location != null ? location.getContinent() : null,
                location != null ? location.getCountry() : null,
                location != null ? location.getRegion() : null,
                location != null ? location.getCountryCode() : null,
                location != null ? location.getIslandGroup() : null,
                location != null ? location.getStateProvince() : null,
                location != null ? location.getWaterBody() : null
        ).anyMatch(StringUtils::isNotBlank);
        boolean hasQuantitativeLocation = event.getDecimalLatitude() != null && event.getDecimalLongitude() != null;
        boolean hasCollectingAgent = StringUtils.isNotBlank(event.getRecordedBy());
        boolean hasDateCollected = StringUtils.isNotBlank(event.getEventDate());
        boolean hasCollectingNumber = StringUtils.isNotBlank(event.getFieldNumber());
        boolean hasMedia = specimen.getMedias() != null && specimen.getMedias().stream()
                .anyMatch(m -> m != null && StringUtils.isNotBlank(m.getMediaUrl()));

        if (!hasQualitativeLocation) {
            missingInformations.add(List.of("locality", "county", "municipality", "island", "continent", "country", "region", "countryCode", "islandGroup", "stateProvince", "waterBody"));
        }

        if (!hasQuantitativeLocation) {
            missingInformations.add(List.of("latitude", "longitude"));
        }

        if (!hasCollectingAgent) {
            missingInformations.add(Collections.singletonList("recordedBy"));
        }

        if (!hasDateCollected) {
            missingInformations.add(Collections.singletonList("eventDate"));
        }

        if (!hasCollectingNumber) {
            missingInformations.add(Collections.singletonList("fieldNumber"));
        }

        if (!hasMedia) {
            missingInformations.add(Collections.singletonList("media"));
        }

        return missingInformations;
    }

    // MIDS-R3 Validation
    private List<List<String>> isMids3Validated(Specimen specimen) {
        // TODO
        return Collections.emptyList();
    }
}
