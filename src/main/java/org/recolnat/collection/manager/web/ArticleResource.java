package org.recolnat.collection.manager.web;

import io.recolnat.api.ArticleApi;
import io.recolnat.model.ArticleDashboardResponseDTO;
import io.recolnat.model.ArticleResponseDTO;
import io.recolnat.model.ArticleStateDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.Article;
import org.recolnat.collection.manager.api.domain.enums.ArticleStatusEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.ArticleMapper;
import org.recolnat.collection.manager.service.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.apache.hc.core5.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class ArticleResource implements ArticleApi {
    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    private static Article buildArticle(UUID id, String title, String content, String author, String creationDate, MultipartFile media, ArticleStateDTO state) {
        return Article.builder()
                .id(id)
                .author(author)
                .content(content)
                .title(title)
                .creationDate(StringUtils.isNotBlank(creationDate) ? LocalDate.parse(creationDate, DateTimeFormatter.ISO_DATE) : null)
                .media(media)
                .state(ArticleStatusEnum.valueOf(state.getValue()))
                .build();
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return ArticleApi.super.getRequest();
    }

    @Override
    public ResponseEntity<ArticleDashboardResponseDTO> getArticles(Integer page, Integer size, String searchTerm, ArticleStateDTO state) {
        try {
            final var result = articleMapper.toDashboardResponseDTO(articleService.findAllForDashboard(page, size, searchTerm, state));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> addArticle(String title, String content, String author, String creationDate, ArticleStateDTO state, MultipartFile media) {
        try {
            var art = buildArticle(null, title, content, author, creationDate, media, state);
            var saved = articleService.save(art);
            var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/" + saved.toString()).build();
            return ResponseEntity.created(uri.toUri()).build();
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ArticleResponseDTO> getArticleById(UUID id) {
        try {
            var dto = articleMapper.toDTO(articleService.getById(id));
            return new ResponseEntity<>(dto, OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> updateArticleById(UUID id, String title, String content, String author, String creationDate, MultipartFile media,
                                                  ArticleStateDTO state) {
        try {
            var art = buildArticle(id, title, content, author, creationDate, media, state);
            var saved = articleService.update(art);
            var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/" + saved.toString()).build();
            return ResponseEntity.ok().header("articleId", saved.toString()).header(LOCATION, uri.toString()).build();
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> deleteArticle(UUID id) {
        try {
            articleService.deleteArticle(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CollectionManagerBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

}
