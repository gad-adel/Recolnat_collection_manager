package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.recolnat.collection.manager.api.domain.enums.SpecimenStatusEnum;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
@Entity(name = "specimen")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@NamedEntityGraph(name = "spec-entity-graph",
        attributeNodes = {@NamedAttributeNode(value = "identifications")})

@NamedEntityGraph(name = "spec-entity-graph-with-taxon",
        attributeNodes = {@NamedAttributeNode(value = "identifications", subgraph = "taxon-subgraph")},
        subgraphs = {
                @NamedSubgraph(
                        name = "taxon-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode(value = "taxon")
                        }
                )
        }
)
@NamedEntityGraph(name = "spec-entity-graph-All",
        attributeNodes = {@NamedAttributeNode(value = "identifications", subgraph = "taxon-subgraph"),
                @NamedAttributeNode(value = "collection", subgraph = "institution-subgraph"), @NamedAttributeNode(value = "geologicalContext"),
                @NamedAttributeNode(value = "collectionEvent"), @NamedAttributeNode(value = "literatures"),
                @NamedAttributeNode(value = "medias"), @NamedAttributeNode(value = "other"), @NamedAttributeNode(value = "management")},
        subgraphs = {
                @NamedSubgraph(
                        name = "taxon-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode(value = "taxon")
                        }
                ),
                @NamedSubgraph(
                        name = "institution-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode(value = "institution")
                        }
                )
        }
)

public class SpecimenJPA extends AbstractEntity {

    private LocalDateTime createdAt;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private String catalogNumber;
    private String recordNumber;
    private String basisOfRecord;
    private String preparations;
    private String individualCount;
    private String sex;
    private String lifeStage;
    private String occurrenceRemarks;
    private String legalStatus;
    private String donor;
    private String collectionCode;
    private LocalDateTime dataChangeTs;
    private String nominativeCollection;
    private Integer mids;

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "FK_ID_SPECIMEN", referencedColumnName = "id")
    @Fetch(value = FetchMode.SUBSELECT)
    @ToString.Include
    private Set<IdentificationJPA> identifications = new HashSet<>();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "FK_ID_COLLECTION", referencedColumnName = "ID", nullable = false)
    private CollectionJPA collection;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "FK_GEO_ID", referencedColumnName = "ID")
    private GeologicalContextJPA geologicalContext;

    @Enumerated(EnumType.STRING)
    private SpecimenStatusEnum state;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "FK_COLEVENT_ID")
    private CollectionEventJPA collectionEvent;

    @OneToMany(fetch = LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "FK_ID_SPECIMEN", referencedColumnName = "ID", nullable = false)
    private Set<LiteratureJPA> literatures = new java.util.LinkedHashSet<>();


    @OneToMany(fetch = LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "FK_ID_SPECIMEN")
    private Set<MediaJPA> medias = new HashSet<>();

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "FK_OTHER_ID", referencedColumnName = "ID")
    private OtherJPA other;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "FK_MANAGEMENT_ID", referencedColumnName = "ID")
    private ManagementJPA management;


    public void addLiterature(Set<LiteratureJPA> literatures) {
        if (CollectionUtils.isEmpty(literatures)) {
            this.literatures = new HashSet<>();
        }
        this.literatures.addAll(literatures);
    }

    public void addIdentifications(Set<IdentificationJPA> identifications) {
        if (identifications == null) {
            this.identifications = new HashSet<>();
        }
        this.identifications.addAll(identifications);
    }

    public void addMedias(List<MediaJPA> medias) {
        if (medias == null) {
            this.medias = new HashSet<>();
        }
        this.medias.addAll(medias);
    }

}
