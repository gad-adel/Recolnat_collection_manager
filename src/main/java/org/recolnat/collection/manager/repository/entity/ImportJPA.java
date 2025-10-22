package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportStatusEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity(name = "import")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ImportJPA extends AbstractEntity {
    private LocalDateTime timestamp;
    private String userName;
    private String email;
    private UUID institutionId;
    @Enumerated(EnumType.STRING)
    private ImportStatusEnum status;
    private long addedSpecimenCount;
    private long updatedSpecimenCount;
    private long addedIdentificationCount;
    private long addedLiteratureCount;

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_import_id", referencedColumnName = "id")
    private List<ImportFileJPA> files = new ArrayList<>();
}
