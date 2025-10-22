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
public class Other extends DomainId {
    private String linkGerBank;
    private String linkBold;
    private String linkOther;
    private String financialAid;
    private String computerizationProgram;
    private String remarks;

}
