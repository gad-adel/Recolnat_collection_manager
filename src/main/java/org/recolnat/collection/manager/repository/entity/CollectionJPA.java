package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
@EqualsAndHashCode
@Getter
@Setter
@Entity(name = "collection")
public class CollectionJPA extends AbstractEntity {
    private String typeCollection;
    private String collectionNameFr;
    private String collectionNameEn;
    private String descriptionFr;
    private String descriptionEn;
    private Integer institutionId;
    private String collectionCode;
    private LocalDateTime dataChangeTs;
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<SpecimenJPA> specimens;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "fk_institution_id", referencedColumnName = "institutionId")
    private InstitutionJPA institution;


}
