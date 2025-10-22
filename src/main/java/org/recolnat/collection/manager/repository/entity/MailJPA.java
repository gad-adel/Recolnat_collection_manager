package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.recolnat.collection.manager.api.domain.enums.MailStatusEnum;

import java.time.LocalDateTime;

@Entity(name = "mail")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class MailJPA extends AbstractEntity {

    @Column(name = "\"from\"")
    private String from;
    @Column(name = "\"to\"")
    private String to;
    private String subject;
    private String cc;
    private String bcc;
    private String content;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private MailStatusEnum state;
}
