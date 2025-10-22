package org.recolnat.collection.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.enums.LevelTypeEnum;
import org.recolnat.collection.manager.repository.entity.IdentificationJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.entity.TaxonJPA;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.imports.ImportIdentificationProcessor;
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
public class ImportIdentificationProcessorITest {

    @Autowired
    private ImportIdentificationProcessor importIdentificationProcessor;

    @Autowired
    private SpecimenJPARepository specimenJPARepository;

    @Sql(scripts = "classpath:import/identification/specimen.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Test
    void handleFile() throws Exception {
        String filePath = "import/identification/data/identification_full_fields.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);

        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");

        List<SpecimenJPA> specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var specimenJPA = specimenJPARepository.findSpecimenById(specimens.get(0).getId()).orElseThrow();
        assertThat(specimenJPA.getIdentifications()).isEmpty();

        importIdentificationProcessor.handleFile(file, institutionId);

        specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        specimenJPA = specimenJPARepository.findSpecimenById(specimens.get(0).getId()).orElseThrow();
        var identifications = specimenJPA.getIdentifications().stream().toList();
        assertThat(identifications).isNotEmpty();
        IdentificationJPA actual = identifications.get(0);
        assertThat(actual.getCurrentDetermination()).isTrue();
        assertThat(actual.getVerbatimIdentification()).isEqualTo("Hecticoceras zieteni");
        assertThat(actual.getIdentificationVerificationStatus()).isFalse();
        assertThat(actual.getTypeStatus()).isEqualTo("Holotype");
        assertThat(actual.getIdentifiedByID()).isEqualTo("Jacques Franklin");
        assertThat(actual.getDateIdentified()).isEqualTo(LocalDate.of(2020, 5, 6));
        assertThat(actual.getIdentificationRemarks()).isEqualTo("Remarques");
        assertThat(actual.getTaxon()).hasSize(1);

        // taxon
        TaxonJPA taxon = actual.getTaxon().get(0);
        assertThat(taxon.getLevelType()).isEqualTo(LevelTypeEnum.MASTER);
        assertThat(taxon.getScientificName()).isEqualTo("Hecticoceras zieteni");
        assertThat(taxon.getScientificNameAuthorship()).isEqualTo("Tsytovitch, 1911");
        assertThat(taxon.getVernacularName()).isEqualTo("Lorem");
        assertThat(taxon.getFamily()).isEqualTo("Oppeliidae");
        assertThat(taxon.getSubFamily()).isEqualTo("Lamiinae");
        assertThat(taxon.getGenus()).isEqualTo("Hecticoceras");
        assertThat(taxon.getSubGenus()).isEqualTo("Delavalia");
        assertThat(taxon.getSpecificEpithet()).isEqualTo("zieteni");
        assertThat(taxon.getInfraspecificEpithet()).isEqualTo("sedoides");
        assertThat(taxon.getKingdom()).isEqualTo("Animalia");
        assertThat(taxon.getPhylum()).isEqualTo("Mollusca");
        assertThat(taxon.getTaxonOrder()).isEqualTo("Ammonitida");
        assertThat(taxon.getSubOrder()).isEqualTo("Sous ordre");
        assertThat(taxon.getTaxonClass()).isEqualTo("Classe");
        assertThat(taxon.getTaxonRemarks()).isEqualTo("Remarques Taxon");
    }

    @Disabled("A lancer pour des benchs pas en CI")
    @Sql(scripts = "classpath:import/identification/specimen.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Test
    void handleFile_20000() throws Exception {
        String filePath = "import/identification/data/identification_20000.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);

        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");

        List<SpecimenJPA> specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var specimenJPA = specimenJPARepository.findSpecimenById(specimens.get(0).getId()).orElseThrow();
        assertThat(specimenJPA.getIdentifications()).isEmpty();

        importIdentificationProcessor.handleFile(file, institutionId);

        specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        specimenJPA = specimenJPARepository.findSpecimenById(specimens.get(0).getId()).orElseThrow();
        var identifications = specimenJPA.getIdentifications().stream().toList();
        assertThat(identifications).hasSize(20000);
    }

    @Sql(scripts = "classpath:import/identification/specimen_with_identification.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Test
    void handleFile_current_determination() throws Exception {
        String filePath = "import/identification/data/identification_current_determination.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);

        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");

        List<SpecimenJPA> specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var specimenJPA = specimenJPARepository.findSpecimenById(specimens.get(0).getId()).orElseThrow();
        assertThat(specimenJPA.getIdentifications()).hasSize(1);

        importIdentificationProcessor.handleFile(file, institutionId);

        specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        specimenJPA = specimenJPARepository.findSpecimenById(specimens.get(0).getId()).orElseThrow();
        var identifications = specimenJPA.getIdentifications().stream().toList();
        assertThat(identifications).hasSize(3);

        List<IdentificationJPA> currentDeterminations = identifications.stream().filter(i -> Boolean.TRUE.equals(i.getCurrentDetermination())).toList();
        assertThat(currentDeterminations).hasSize(1);
        assertThat(currentDeterminations.get(0).getVerbatimIdentification()).isEqualTo("Verbatim");
    }
}
