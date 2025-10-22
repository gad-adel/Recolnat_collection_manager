package org.recolnat.collection.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.repository.jpa.IdentificationJPARepository;
import org.recolnat.collection.manager.repository.jpa.ImportJPARepository;
import org.recolnat.collection.manager.repository.jpa.LiteratureJPARepository;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.imports.ImportProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("int")
@Slf4j
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ImportProcessorITest {

    @Autowired
    private ImportProcessor importProcessor;
    @Autowired
    private SpecimenJPARepository specimenJPARepository;
    @Autowired
    private IdentificationJPARepository identificationJPARepository;
    @Autowired
    private LiteratureJPARepository literatureJPARepository;
    @Autowired
    private ImportJPARepository importJPARepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(importProcessor, "baseDirectory", Paths.get("src", "test", "resources").toString());
    }

    @Sql(scripts = "classpath:import/data/handleImport.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Test
    void handleImport_specimen_ok() {
        var count = specimenJPARepository.count();
        assertThat(count).isEqualTo(0);

        var importJPA = importJPARepository.findById(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc")).orElseThrow();
        importProcessor.handleImport(importJPA);
        importJPA = importJPARepository.findById(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc")).orElseThrow();

        assertThat(importJPA.getAddedSpecimenCount()).isEqualTo(1);
        assertThat(importJPA.getAddedIdentificationCount()).isEqualTo(1);
        assertThat(importJPA.getAddedLiteratureCount()).isEqualTo(1);

        assertThat(specimenJPARepository.count()).isEqualTo(1);
        assertThat(identificationJPARepository.count()).isEqualTo(2);
        assertThat(literatureJPARepository.count()).isEqualTo(1);
    }

    @Sql(scripts = "classpath:import/data/handleImport_ko.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Test
    void handleImport_specimen_collection_dont_exists() {
        var count = specimenJPARepository.count();
        assertThat(count).isEqualTo(0);

        var importJPA = importJPARepository.findById(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc")).orElseThrow();
        importProcessor.handleImport(importJPA);
        count = specimenJPARepository.count();
        assertThat(count).isEqualTo(0);
        importJPA = importJPARepository.findById(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc")).orElseThrow();

        assertThat(importJPA.getAddedSpecimenCount()).isEqualTo(0);
        assertThat(importJPA.getAddedIdentificationCount()).isEqualTo(0);
        assertThat(importJPA.getAddedLiteratureCount()).isEqualTo(0);
        assertThat(importJPA.getUpdatedSpecimenCount()).isEqualTo(0);
    }

    @Disabled("A lancer pour des benchs pas en CI")
    @Sql(scripts = "classpath:import/data/handleImport_20000.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Test
    void handleImport_20000() {
        assertThat(specimenJPARepository.count()).isEqualTo(0);
        assertThat(identificationJPARepository.count()).isEqualTo(0);
        assertThat(literatureJPARepository.count()).isEqualTo(0);

        var importJPA = importJPARepository.findById(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abd")).orElseThrow();
        importProcessor.handleImport(importJPA);
        importJPA = importJPARepository.findById(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abd")).orElseThrow();

        assertThat(importJPA.getAddedSpecimenCount()).isEqualTo(20000);
        assertThat(importJPA.getAddedIdentificationCount()).isEqualTo(20000);
        assertThat(importJPA.getAddedLiteratureCount()).isEqualTo(20000);
        assertThat(importJPA.getUpdatedSpecimenCount()).isEqualTo(0);

        assertThat(specimenJPARepository.count()).isEqualTo(20000);
        assertThat(identificationJPARepository.count()).isEqualTo(40000);
        assertThat(literatureJPARepository.count()).isEqualTo(20000);
    }
}
