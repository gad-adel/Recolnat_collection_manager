package org.recolnat.collection.manager.api.domain;

import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class ArticleResult {
    private int totalPages;
    private List<Article> articles;
}
