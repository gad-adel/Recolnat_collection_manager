package org.recolnat.collection.manager.api.domain;

import lombok.Builder;
import lombok.Data;


import java.util.List;

@Data
@Builder
public class InstitutionPage {

    private Integer totalPages;
    private List<Institution> institutions;
}
