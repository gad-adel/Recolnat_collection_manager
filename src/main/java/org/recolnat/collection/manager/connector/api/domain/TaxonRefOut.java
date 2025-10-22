package org.recolnat.collection.manager.connector.api.domain;

import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TaxonRefOut {
    @JsonAlias({"taxons"})
    private List<TaxonRef> taxonList;
}
