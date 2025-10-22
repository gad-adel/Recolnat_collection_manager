package org.recolnat.collection.manager.api.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
@EqualsAndHashCode
public class Literature extends DomainId {

    private String identifier;

    private String authors;

    private String date; // Date au format YYYY

    private String title;

    private String review;

    private String volume;

    private String number;

    private String pages;

    private String publisher;

    private String publicationPlace;

    private String editors;

    private String bookTitle;

    private String pageNumber;

    private String citation;

    private String language;

    private String keywords;

    private String description;

    private String url;

    private String remarks;

}
