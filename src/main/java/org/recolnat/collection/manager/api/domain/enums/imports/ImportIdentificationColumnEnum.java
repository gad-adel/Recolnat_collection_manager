package org.recolnat.collection.manager.api.domain.enums.imports;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ImportIdentificationColumnEnum {
    // identification
    COLLECTION_NAME("NOM_COLLECTION"),
    CATALOG_NUMBER("NUMERO_INVENTAIRE"),
    CURRENT_DETERMINATION("ACTUELLE_DETERMINATION"),
    VERBATIM_IDENTIFICATION("VERBATIM_DETERMINATION"),
    IDENTIFICATION_VERIFICATION_STATUS("DOUTE_DETERMINATION"),
    IDENTIFICATION_REMARKS("REMARQUES_DETERMINATION"),
    TYPE_STATUS("TYPE"),
    IDENTIFIED_BYID("AUTEUR_DETERMINATION"),
    DATE_IDENTIFIED_YEAR("DETERMINATION_ANNEE"),
    DATE_IDENTIFIED_MONTH("DETERMINATION_MOIS"),
    DATE_IDENTIFIED_DAY("DETERMINATION_JOUR"),
    // taxon
    SCIENTIFIC_NAME("NOM_SCIENTIFIQUE"),
    SCIENTIFIC_NAME_AUTHORSHIP("AUTEURS_TAXON"),
    VERNACULAR_NAME("NOM_VERNACULAIRE"),
    FAMILY("FAMILLE"),
    SUB_FAMILY("SOUS_FAMILLE"),
    GENUS("GENRE"),
    SUB_GENUS("SOUS_GENRE"),
    SPECIFIC_EPITHET("EPITHETE_SPECIFIQUE"),
    INFRASPECIFIC_EPITHET("EPITHETE_INFRA_SPECIFIQUE"),
    KINGDOM("REGNE"),
    PHYLUM("EMBRANCHEMENT"),
    TAXON_ORDER("ORDRE"),
    TAXON_CLASS("CLASSE"),
    SUB_ORDER("SOUS_ORDRE"),
    TAXON_REMARKS("REMARQUES_TAXON");


    private final String columnName;

    ImportIdentificationColumnEnum(String columnName) {
        this.columnName = columnName;
    }


    public static ImportIdentificationColumnEnum fromValue(String value) {
        for (ImportIdentificationColumnEnum b : values()) {
            if (Objects.equals(b.getColumnName(), value)) {
                return b;
            }
        }
        return null;
    }

}
