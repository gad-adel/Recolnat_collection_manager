package org.recolnat.collection.manager.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SpecimenPage {

    private Integer numberOfElements;
    private Integer totalPages;
    private List<Specimen> specimen;
}
