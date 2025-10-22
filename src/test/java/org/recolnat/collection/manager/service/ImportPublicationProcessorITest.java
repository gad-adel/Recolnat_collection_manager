package org.recolnat.collection.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.repository.entity.LiteratureJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.jpa.LiteratureJPARepository;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.imports.ImportPublicationProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("int")
@Slf4j
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ImportPublicationProcessorITest {

    @Autowired
    private ImportPublicationProcessor importPublicationProcessor;

    @Autowired
    private SpecimenJPARepository specimenJPARepository;

    @Autowired
    private LiteratureJPARepository literatureJPARepository;

    @Sql(scripts = "classpath:import/publication/specimen.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Test
    void handleFile() throws Exception {
        String filePath = "import/publication/data/publication_full_fields.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);

        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");

        List<SpecimenJPA> specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var specimenJPA = specimenJPARepository.findSpecimenById(specimens.get(0).getId()).orElseThrow();
        assertThat(specimenJPA.getLiteratures()).isEmpty();

        importPublicationProcessor.handleFile(file, institutionId);

        specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        specimenJPA = specimenJPARepository.findSpecimenById(specimens.get(0).getId()).orElseThrow();
        var identifications = specimenJPA.getLiteratures().stream().toList();
        assertThat(identifications).isNotEmpty();
        LiteratureJPA literature = identifications.get(0);
        assertThat(literature.getIdentifier()).isEqualTo("10.1127/pala/2019/0083");
        assertThat(literature.getUrl()).isEqualTo("https://digitalcommons.unl.edu/insectamundi/1570/");
        assertThat(literature.getCitation()).isEqualTo("Lingafelter SW, Woodley NE. 2024. New species, new combinations, synonymies, and nomenclatural discussion for Hispaniolan longhorned beetles (Coleoptera: Disteniidae, Cerambycidae). Insecta Mundi 1069: 1–41.");
        assertThat(literature.getTitle()).isEqualTo("New Species, New Combinations, Synonymies, and Nomenclatural Discussion for Hispaniolan Longhorned Beetles (Coleoptera: Disteniidae, Cerambycidae)");
        assertThat(literature.getAuthors()).isEqualTo("Lingafelter, S. W. & Woodley, N. E");
        assertThat(literature.getDate()).isEqualTo(LocalDate.of(1069, 1, 1));
        assertThat(literature.getLanguage()).isEqualTo("fr");
        assertThat(literature.getKeywords()).isEqualTo("botanique");
        assertThat(literature.getDescription()).isEqualTo("Plantarum sinensium ecloge tertia");
        assertThat(literature.getRemarks()).isEqualTo("p. 258, pl. 79, fig. 6");
        assertThat(literature.getReview()).isEqualTo("Insecta Mundi");
        assertThat(literature.getVolume()).isEqualTo("1");
        assertThat(literature.getNumber()).isEqualTo("2");
        assertThat(literature.getPages()).isEqualTo("1-41");
        assertThat(literature.getBookTitle()).isEqualTo("Titre ouvrage");
        assertThat(literature.getPublisher()).isEqualTo("maison edition");
        assertThat(literature.getPublicationPlace()).isEqualTo("lieu edition");
        assertThat(literature.getEditors()).isEqualTo("editeurs");
        assertThat(literature.getPageNumber()).isEqualTo("234");
    }

    @Disabled("A lancer pour des benchs pas en CI")
    @Sql(scripts = "classpath:import/publication/specimen.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Test
    void handleFile_20000() throws Exception {
        String filePath = "import/publication/data/publication_20000.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);

        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");

        List<SpecimenJPA> specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var specimenJPA = specimenJPARepository.findSpecimenById(specimens.get(0).getId()).orElseThrow();
        assertThat(specimenJPA.getIdentifications()).isEmpty();

        importPublicationProcessor.handleFile(file, institutionId);

        specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var literatures = literatureJPARepository.findAllBySpecimenId(specimens.get(0).getId());
        assertThat(literatures).hasSize(20000);
    }
}
