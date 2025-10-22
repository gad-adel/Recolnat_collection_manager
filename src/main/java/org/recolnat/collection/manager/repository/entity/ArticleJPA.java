package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import org.recolnat.collection.manager.api.domain.enums.ArticleStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "article")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class ArticleJPA {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(unique = true, nullable = false)
    private Integer incrementId;
    private String author;
    private String title;
    private String content;
    private String urlMedia;
    private LocalDateTime createdAt;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private LocalDate creationDate;
    private LocalDateTime dataChangeTs;
    @Enumerated(EnumType.STRING)
    private ArticleStatusEnum state;
}
