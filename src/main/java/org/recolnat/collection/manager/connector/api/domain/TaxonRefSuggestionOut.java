package org.recolnat.collection.manager.connector.api.domain;

import lombok.*;

import java.util.List;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TaxonRefSuggestionOut {
    private Integer total;
    private String prefix;
    private List<String> suggestions;
}
