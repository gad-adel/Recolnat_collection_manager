package org.recolnat.collection.manager.service.impl;

import io.recolnat.model.ArticleDashboardDTO;
import io.recolnat.model.ArticleStateDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.Article;
import org.recolnat.collection.manager.api.domain.ArticleSearchResult;
import org.recolnat.collection.manager.api.domain.ConnectedUser;
import org.recolnat.collection.manager.api.domain.Result;
import org.recolnat.collection.manager.api.domain.enums.ArticleStatusEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.mapper.ArticleMapper;
import org.recolnat.collection.manager.connector.api.MediathequeService;
import org.recolnat.collection.manager.repository.entity.ArticleJPA;
import org.recolnat.collection.manager.repository.jpa.ArticleJPARepository;
import org.recolnat.collection.manager.service.ArticleService;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.recolnat.collection.manager.service.JpaQueryUtils.likeIgnoreCase;


@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {
    public static final String INVALID_REQUEST = "ERR_CODE_INVALID_REQUEST";
    public static final String RESOURCE_NOT_FOUND_WITH_ID = "Resource not found with id :";

    private final ArticleJPARepository articleJPARepository;
    private final MediathequeService mediathequeApiClient;
    private final Validator validator;
    private final AuthenticationService authenticationService;
    private final ArticleMapper articleMapper;

    @PersistenceContext
    private EntityManager em;


    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public UUID save(Article article) {
        final var user = authenticationService.getConnected();
        //update or create
        if (Objects.nonNull(article.getId())) {
            getArticleJPA(article.getId());
        }
        validate(article);
        var url = Objects.nonNull(article.getMedia()) ? getMediaUrl(article) : null;
        var toSave = buildArticleJPA(article, user, url);
        return articleJPARepository.save(toSave).getId();

    }

    /**
     * Article update with new url media or without (keep, old media)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public UUID update(Article article) {
        final var user = authenticationService.getConnected();
        validate(article);
        ArticleJPA databaseArticle = getArticleJPA(article.getId());

        String url = Objects.nonNull(article.getMedia()) ? getMediaUrl(article) : databaseArticle.getUrlMedia();

        var toSave = buildArticleJPA(article, user, url);
        return articleJPARepository.save(toSave).getId();
    }

    private String getMediaUrl(Article article) {

        try {
            if (Objects.nonNull(article.getMedia())) {
                var saveLogo = mediathequeApiClient.savePicture(article.getMedia());
                if (Objects.isNull(saveLogo) ||
                    Objects.isNull(saveLogo.getBody())) {
                    throw new CollectionManagerBusinessException(ErrorCode.ERR_NFE_CODE, "Media don't found");
                }
                return Objects.nonNull(saveLogo.getBody().getMedia()) ? saveLogo.getBody().getMedia().getUrl() : null;
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CollectionManagerBusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "MEDIATHEQUE_ERR_CODE", e.getMessage());
        }
        return null;
    }

    private void validate(Article article) {
        //validate
        final var errors = validator.validate(article);
        if (!CollectionUtils.isEmpty(errors)) {
            final String errMsg = errors.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
            log.error("invalid input : {}", errMsg);
            throw new CollectionManagerBusinessException(
                    HttpStatus.BAD_REQUEST,
                    INVALID_REQUEST,
                    errMsg);
        }
    }

    @Override
    public Article getById(UUID id) {
        ArticleJPA artJPA = articleJPARepository.findById(id).orElseThrow(() -> {
            final var exception = new CollectionManagerBusinessException(
                    HttpStatus.NOT_FOUND,
                    INVALID_REQUEST,
                    RESOURCE_NOT_FOUND_WITH_ID + id);

            log.error(exception.getMessage(), exception);
            return exception;
        });
        return articleMapper.toArticle(artJPA);
    }

    @Override
    public Article getPublicArticleById(UUID id) {
        ArticleJPA artJPA = articleJPARepository.findByIdAndState(id, ArticleStatusEnum.PUBLISHED).orElseThrow(() -> {
            final var exception = new CollectionManagerBusinessException(
                    HttpStatus.NOT_FOUND,
                    INVALID_REQUEST,
                    RESOURCE_NOT_FOUND_WITH_ID + id);

            log.error(exception.getMessage(), exception);
            return exception;
        });
        return articleMapper.toArticle(artJPA);
    }

    private ArticleJPA getArticleJPA(UUID id) {
        return articleJPARepository.findById(id).orElseThrow(() -> {
            final var exception = new CollectionManagerBusinessException(
                    HttpStatus.NOT_FOUND,
                    INVALID_REQUEST,
                    RESOURCE_NOT_FOUND_WITH_ID + id);

            log.error(exception.getMessage(), exception);
            return exception;
        });
    }

    private ArticleJPA buildArticleJPA(Article article, ConnectedUser user, String url) {
        log.info("Get article : {}", article);
        return ArticleJPA.builder().id(article.getId())
                .title(article.getTitle())
                .author(article.getAuthor())
                .content(article.getContent())
                .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                .createdBy(user.getUserName())
                .creationDate(article.getCreationDate()).incrementId(getNextVal())
                .modifiedBy(user.getUserName())
                .modifiedAt(LocalDateTime.now(ZoneId.of("UTC")))
                .state(article.getState())
                .urlMedia(url).build();
    }


    private int getNextVal() {
        var count = Math.toIntExact(articleJPARepository.count());
        return new AtomicInteger(count).incrementAndGet();
    }

    @Override
    public ArticleSearchResult findAll(Pageable p) {
        Page<ArticleJPA> jpas = articleJPARepository.findAllByState(ArticleStatusEnum.PUBLISHED, p);
        if (jpas.hasContent()) {
            var articles = jpas.getContent().stream().map(articleMapper::toArticle).toList();
            return ArticleSearchResult.builder().articles(articles).totalPages(jpas.getTotalPages())
                    .numberOfElements(articles.size()).build();
        }
        return ArticleSearchResult.builder().articles(List.of()).totalPages(0)
                .numberOfElements(0).build();
    }


    @Override
    public Result<ArticleDashboardDTO> findAllForDashboard(Integer page, Integer size, String searchTerm, ArticleStateDTO state) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<ArticleJPA> root = query.from(ArticleJPA.class);

        query.multiselect(
                root.get("id"),
                root.get("title"),
                root.get("author"),
                root.get("creationDate"),
                root.get("state")
        );

        List<Order> sorting = List.of(cb.desc(root.get("createdAt")), cb.asc(root.get("title")));

        ArrayList<Predicate> filters = new ArrayList<>(getFilters(cb, root, searchTerm, state));

        query.where(cb.and(filters.toArray(new Predicate[0]))).orderBy(sorting);

        TypedQuery<Tuple> tuples = em.createQuery(query);
        tuples.setFirstResult(page * size).setMaxResults(size);
        List<ArticleDashboardDTO> articles = getArticleFromTuples(tuples);

        Long total = countArticles(searchTerm, state);

        var pageable = new PageImpl<>(articles, PageRequest.of(page, size), total);
        return Result.<ArticleDashboardDTO>builder().data(articles).numberOfElements(total).totalPages(pageable.getTotalPages()).build();
    }

    private Long countArticles(String searchTerm, ArticleStateDTO state) {
        CriteriaBuilder cbCount = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQueryCount = cbCount.createQuery(Long.class);
        Root<ArticleJPA> rootCount = criteriaQueryCount.from(ArticleJPA.class);

        criteriaQueryCount.select(cbCount.count(rootCount.get("id")));

        ArrayList<Predicate> filters = new ArrayList<>(getFilters(cbCount, rootCount, searchTerm, state));

        criteriaQueryCount.where(cbCount.and(filters.toArray(new Predicate[0])));

        TypedQuery<Long> totalQuery = em.createQuery(criteriaQueryCount);
        return totalQuery.getSingleResult();
    }

    private List<ArticleDashboardDTO> getArticleFromTuples(TypedQuery<Tuple> tuples) {
        List<Tuple> objectsArray = tuples.getResultList();

        return objectsArray.stream().map(objetArray -> {
            ArticleDashboardDTO dto = new ArticleDashboardDTO();
            dto.setId((UUID) objetArray.get(0));
            dto.setTitle((String) objetArray.get(1));
            dto.setAuthor((String) objetArray.get(2));
            dto.setCreationDate((LocalDate) objetArray.get(3));
            dto.setState(ArticleStateDTO.valueOf(((ArticleStatusEnum) objetArray.get(4)).name()));
            return dto;
        }).toList();
    }

    private List<Predicate> getFilters(CriteriaBuilder cb, Root<ArticleJPA> root, String searchTerm, ArticleStateDTO state) {
        ArrayList<Predicate> filters = new ArrayList<>();

        if (StringUtils.isNotBlank(searchTerm)) {
            filters.add(likeIgnoreCase(cb, root.get("title"), searchTerm));
        }

        if (state != null) {
            filters.add(cb.equal(root.get("state"), ArticleStatusEnum.valueOf(state.getValue())));
        }

        return filters;
    }

    @Override
    public void deleteArticle(UUID id) {
        final var article = getArticleJPA(id);
        articleJPARepository.delete(article);
    }
}
