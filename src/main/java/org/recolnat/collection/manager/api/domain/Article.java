package org.recolnat.collection.manager.api.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.recolnat.collection.manager.api.domain.enums.ArticleStatusEnum;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {
    private UUID id;
    @NotBlank(message = "author is required field")
    private String author;
    @NotBlank(message = "title is required field")
    private String title;
    private String urlMedia;
    private String content;
    private MultipartFile media;
    private LocalDateTime createdAt;
    private String createdBy;
    @NotNull(message = "date of Creation is required")
    private LocalDate creationDate;
    private Integer incrementId;
    private ArticleStatusEnum state;
}
