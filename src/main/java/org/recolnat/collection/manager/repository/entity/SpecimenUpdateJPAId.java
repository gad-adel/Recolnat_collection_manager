package org.recolnat.collection.manager.repository.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SpecimenUpdateJPAId implements Serializable {
    private ImportJPA importJPA;
    private SpecimenJPA specimenJPA;
}
