package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity(name = "other")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class OtherJPA extends AbstractEntity {
    private String linkGerBank;
    private String linkBold;
    private String linkOther;
    private String financialAid;
    private String computerizationProgram;
    private String remarks;

}
