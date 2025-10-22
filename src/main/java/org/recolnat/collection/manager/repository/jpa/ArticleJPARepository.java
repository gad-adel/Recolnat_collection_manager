package org.recolnat.collection.manager.repository.jpa;


import org.recolnat.collection.manager.api.domain.enums.ArticleStatusEnum;
import org.recolnat.collection.manager.repository.entity.ArticleJPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ArticleJPARepository extends JpaRepository<ArticleJPA, UUID> {
    @Query("SELECT a FROM article a WHERE a.state = :state ORDER BY a.creationDate DESC")
    Page<ArticleJPA> findAllByState(ArticleStatusEnum state, Pageable pageable);

    Optional<ArticleJPA> findByIdAndState(UUID id, ArticleStatusEnum articleStatusEnum);
}
