package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.recolnat.collection.manager.api.domain.enums.imports.SpecimenUpdateModeEnum;

import static jakarta.persistence.FetchType.LAZY;

@IdClass(SpecimenUpdateJPAId.class)
@Entity(name = "specimenUpdate")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SpecimenUpdateJPA {

    @Id
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "fk_import_id", referencedColumnName = "id")
    private ImportJPA importJPA;

    @Id
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "fk_specimen_id", referencedColumnName = "id")
    private SpecimenJPA specimenJPA;

    @Enumerated(EnumType.STRING)
    private SpecimenUpdateModeEnum mode;
}
