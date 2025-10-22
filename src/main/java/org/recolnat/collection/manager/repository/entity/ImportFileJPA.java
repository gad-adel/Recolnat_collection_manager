package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportFileType;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportModeEnum;

import static jakarta.persistence.FetchType.LAZY;

@Entity(name = "importFile")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ImportFileJPA extends AbstractEntity {


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "fk_import_id", referencedColumnName = "id")
    private ImportJPA importJPA;

    @Enumerated(EnumType.STRING)
    private ImportFileType fileType;

    private String fileName;
    private long lineCount;
    @Enumerated(EnumType.STRING)
    private ImportModeEnum mode;
}
