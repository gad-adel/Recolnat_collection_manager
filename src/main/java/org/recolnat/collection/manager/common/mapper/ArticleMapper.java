package org.recolnat.collection.manager.common.mapper;


import io.recolnat.model.ArticleDashboardDTO;
import io.recolnat.model.ArticleDashboardResponseDTO;
import io.recolnat.model.ArticleResponseDTO;
import io.recolnat.model.ArticleResultPageResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.recolnat.collection.manager.api.domain.Article;
import org.recolnat.collection.manager.api.domain.ArticleSearchResult;
import org.recolnat.collection.manager.api.domain.Result;
import org.recolnat.collection.manager.repository.entity.ArticleJPA;

@Mapper(componentModel = "spring")
public interface ArticleMapper {
    @Mapping(target = "media", ignore = true)
    ArticleResponseDTO toDTO(Article article);

    ArticleResultPageResponseDTO toResultArticleDTO(ArticleSearchResult articleResult);

    @Mapping(target = "media", ignore = true)
    Article toArticle(ArticleJPA artJPA);

    @Mapping(target = "data", source = "data")
    @Mapping(target = "numberOfElements", source = "numberOfElements")
    @Mapping(target = "totalPages", source = "totalPages")
    ArticleDashboardResponseDTO toDashboardResponseDTO(Result<ArticleDashboardDTO> allForDashboard);
}
