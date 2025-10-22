package org.recolnat.collection.manager.api.domain;

import lombok.*;

import java.util.List;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CollectionPageResult {
    private Integer totalPages;
    private List<Collection> collections;
}
