package org.recolnat.collection.manager.api.domain.enums.imports;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ImportPublicationColumnEnum {
    COLLECTION_NAME("NOM_COLLECTION"),
    CATALOG_NUMBER("NUMERO_INVENTAIRE"),
    IDENTIFIER("DOI"),
    URL("URL"),
    CITATION("CITATION"),
    TITLE("TITRE_PUBLICATION"),
    AUTHORS("AUTEURS_PUBLICATION"),
    DATE("ANNEE_PUBLICATION"),
    LANGUAGE("LANGUE_PUBLICATION"),
    KEYWORDS("MOTS_CLE"),
    DESCRIPTION("DESCRIPTION_PUBLICATION"),
    REMARKS("REMARQUES_PUBLICATION"),
    REVIEW("TITRE_REVUE"),
    VOLUME("VOLUME_REVUE"),
    NUMBER("NUMERO_REVUE"),
    PAGES("PAGES_CITATION"),
    BOOK_TITLE("TITRE_OUVRAGE"),
    PUBLISHER("MAISON_EDITION_OUVRAGE"),
    PUBLICATION_PLACE("LIEU_EDITION_OUVRAGE"),
    EDITORS("EDITEURS_OUVRAGE"),
    PAGE_NUMBER("NB_PAGES_OUVRAGE");

    private final String columnName;

    ImportPublicationColumnEnum(String columnName) {
        this.columnName = columnName;
    }


    public static ImportPublicationColumnEnum fromValue(String value) {
        for (ImportPublicationColumnEnum b : values()) {
            if (Objects.equals(b.getColumnName(), value)) {
                return b;
            }
        }
        return null;
    }

}
