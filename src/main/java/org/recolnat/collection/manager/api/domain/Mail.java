package org.recolnat.collection.manager.api.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.recolnat.collection.manager.api.domain.enums.MailStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mail {
    private UUID id;
    @NotBlank(message = "from is required")
    private String from;
    @NotBlank(message = "to is required")
    private String to;
    @NotBlank(message = "subject is required")
    private String subject;
    private String cc;
    private String bcc;
    @NotNull(message = "content is required")
    private String content;
    @NotNull(message = "createdAt is required")
    private LocalDateTime createdAt;
    @NotNull(message = "state is required")
    private MailStatusEnum state;
}
