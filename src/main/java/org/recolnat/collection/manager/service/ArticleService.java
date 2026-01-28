package org.recolnat.collection.manager.service;

import io.recolnat.model.ArticleDashboardDTO;
import io.recolnat.model.ArticleStateDTO;
import org.recolnat.collection.manager.api.domain.Article;
import org.recolnat.collection.manager.api.domain.ArticleSearchResult;
import org.recolnat.collection.manager.api.domain.Result;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ArticleService {
    UUID save(Article build);

    UUID update(Article build);

    Article getById(UUID id);

    Article getPublicArticleById(UUID id);

    ArticleSearchResult findAll(Pageable p);

    Result<ArticleDashboardDTO> findAllForDashboard(Integer page, Integer size, String searchTerm, ArticleStateDTO state);

    void deleteArticle(UUID id);
}
