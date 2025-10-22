package org.recolnat.collection.manager.api.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;


/**
 * rem; en substitue de @Setter(onMethod = @__(@JsonSetter(value = )), vous pouvez employer @com.fasterxml.jackson.annotation.JsonProperty ou @JsonAlias
 */
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class SpecimenIndex {


    @Setter(onMethod = @__(@JsonSetter(value = "id")))
    @Getter(onMethod = @__(@JsonGetter(value = "id")))
    private String id;

    @Setter(onMethod = @__(@JsonSetter(value = "institution_id")))
    @Getter(onMethod = @__(@JsonGetter(value = "institution_id")))
    private UUID institutionId;

    @Setter(onMethod = @__(@JsonSetter(value = "institution_name")))
    @Getter(onMethod = @__(@JsonGetter(value = "institution_name")))
    private String institutionName;

    @Setter(onMethod = @__(@JsonSetter(value = "institution_logo_url")))
    @Getter(onMethod = @__(@JsonGetter(value = "institution_logo_url")))
    private String institutionLogoUrl;

    @Setter(onMethod = @__(@JsonSetter(value = "collection_id")))
    @Getter(onMethod = @__(@JsonGetter(value = "collection_id")))
    private String collectionId;

    @Setter(onMethod = @__(@JsonSetter(value = "collection_code")))
    @Getter(onMethod = @__(@JsonGetter(value = "collection_code")))
    private String collectionCode;

    @Setter(onMethod = @__(@JsonSetter(value = "collection_name_fr")))
    @Getter(onMethod = @__(@JsonGetter(value = "collection_name_fr")))
    private String collectionNameFr;

    @Setter(onMethod = @__(@JsonSetter(value = "collection_name_en")))
    @Getter(onMethod = @__(@JsonGetter(value = "collection_name_en")))
    private String collectionNameEn;

    @Setter(onMethod = @__(@JsonSetter(value = "catalog_number")))
    @Getter(onMethod = @__(@JsonGetter(value = "catalog_number")))
    private String catalogNumber;

    @Setter(onMethod = @__(@JsonSetter(value = "nominative_collection")))
    @Getter(onMethod = @__(@JsonGetter(value = "nominative_collection")))
    private String nominativeCollection;

    @Setter(onMethod = @__(@JsonSetter(value = "domain")))
    @Getter(onMethod = @__(@JsonGetter(value = "domain")))
    private String domain;

    @Setter(onMethod = @__(@JsonSetter(value = "scientific_names")))
    @Getter(onMethod = @__(@JsonGetter(value = "scientific_names")))
    private String[] scientificNames;

    @Setter(onMethod = @__(@JsonSetter(value = "scientific_name_authorships")))
    @Getter(onMethod = @__(@JsonGetter(value = "scientific_name_authorships")))
    private String[] scientificNameAuthorships;

    @Setter(onMethod = @__(@JsonSetter(value = "genus")))
    @Getter(onMethod = @__(@JsonGetter(value = "genus")))
    private String[] genus;

    @Setter(onMethod = @__(@JsonSetter(value = "vernacular_names")))
    @Getter(onMethod = @__(@JsonGetter(value = "vernacular_names")))
    private String[] vernacularName;

    @Setter(onMethod = @__(@JsonSetter(value = "identification_by_ids")))
    @Getter(onMethod = @__(@JsonGetter(value = "identification_by_ids")))
    private String[] identificationByIds;

    @Setter(onMethod = @__(@JsonSetter(value = "types_status")))
    @Getter(onMethod = @__(@JsonGetter(value = "types_status")))
    private String[] typesStatus;

    @Setter(onMethod = @__(@JsonSetter(value = "continent")))
    @Getter(onMethod = @__(@JsonGetter(value = "continent")))
    private String continent;

    @Setter(onMethod = @__(@JsonSetter(value = "county")))
    @Getter(onMethod = @__(@JsonGetter(value = "county")))
    private String county;

    @Setter(onMethod = @__(@JsonSetter(value = "municipality")))
    @Getter(onMethod = @__(@JsonGetter(value = "municipality")))
    private String municipality;

    @Setter(onMethod = @__(@JsonSetter(value = "country")))
    @Getter(onMethod = @__(@JsonGetter(value = "country")))
    private String country;

    @Setter(onMethod = @__(@JsonSetter(value = "water_body")))
    @Getter(onMethod = @__(@JsonGetter(value = "water_body")))
    private String waterBody;

    @Setter(onMethod = @__(@JsonSetter(value = "island")))
    @Getter(onMethod = @__(@JsonGetter(value = "island")))
    private String island;

    @Setter(onMethod = @__(@JsonSetter(value = "island_group")))
    @Getter(onMethod = @__(@JsonGetter(value = "island_group")))
    private String islandGroup;

    @Setter(onMethod = @__(@JsonSetter(value = "region")))
    @Getter(onMethod = @__(@JsonGetter(value = "region")))
    private String region;

    @Setter(onMethod = @__(@JsonSetter(value = "locality")))
    @Getter(onMethod = @__(@JsonGetter(value = "locality")))
    private String locality;

    @Setter(onMethod = @__(@JsonSetter(value = "recorded_by")))
    @Getter(onMethod = @__(@JsonGetter(value = "recorded_by")))
    private String recordedBy;

    @Setter(onMethod = @__(@JsonSetter(value = "media_url")))
    @Getter(onMethod = @__(@JsonGetter(value = "media_url")))
    private String mediaUrl;

    @Setter(onMethod = @__(@JsonSetter(value = "decimal_latitude")))
    @Getter(onMethod = @__(@JsonGetter(value = "decimal_latitude")))
    private Float decimalLatitude;

    @Setter(onMethod = @__(@JsonSetter(value = "decimal_longitude")))
    @Getter(onMethod = @__(@JsonGetter(value = "decimal_longitude")))
    private Float decimalLongitude;

    @Setter(onMethod = @__(@JsonSetter(value = "field_number")))
    @Getter(onMethod = @__(@JsonGetter(value = "field_number")))
    private String fieldNumber;

    @Setter(onMethod = @__(@JsonSetter(value = "collection_date")))
    @Getter(onMethod = @__(@JsonGetter(value = "collection_date")))
    private String collectionDate;

    @Setter(onMethod = @__(@JsonSetter(value = "specific_epithet")))
    @Getter(onMethod = @__(@JsonGetter(value = "specific_epithet")))
    private String[] specificEpithet;

    @Setter(onMethod = @__(@JsonSetter(value = "family")))
    @Getter(onMethod = @__(@JsonGetter(value = "family")))
    private String[] family;

    @Setter(onMethod = @__(@JsonSetter(value = "geojson")))
    @Getter(onMethod = @__(@JsonGetter(value = "geojson")))
    private Float[] geojson;

}
