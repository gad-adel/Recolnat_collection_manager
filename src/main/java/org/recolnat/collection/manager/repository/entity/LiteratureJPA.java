package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * id uuid not null,
 * authors varchar(255),
 * book_title varchar(255),
 * citation varchar(255),
 * date date,
 * description varchar(255),
 * editors varchar(255),
 * identifier varchar(255),
 * keywords varchar(255),
 * language varchar(255),
 * number varchar(255),
 * page_number varchar(255),
 * pages varchar(255),
 * publication_place varchar(255),
 * publisher varchar(255),
 * remarks varchar(255),
 * review varchar(255),
 * title varchar(255),
 * url varchar(255),
 * volume varchar(255),
 * fk_id_specimen uuid not null,
 * primary key (id)
 **/
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
@Entity(name = "literature")
@Getter
@Setter
@EqualsAndHashCode
public class LiteratureJPA extends AbstractEntity {

    private String identifier;

    private String authors;

    private LocalDate date;

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
