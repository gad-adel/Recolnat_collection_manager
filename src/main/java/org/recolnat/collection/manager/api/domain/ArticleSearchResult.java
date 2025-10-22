package org.recolnat.collection.manager.api.domain;

import lombok.*;


import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class ArticleSearchResult {
    private int totalPages;
    private int numberOfElements;
    private List<Article> articles;

}
