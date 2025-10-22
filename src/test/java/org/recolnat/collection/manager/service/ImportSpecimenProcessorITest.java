package org.recolnat.collection.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.enums.LevelTypeEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportModeEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.SpecimenUpdateModeEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.repository.entity.CollectionEventJPA;
import org.recolnat.collection.manager.repository.entity.GeologicalContextJPA;
import org.recolnat.collection.manager.repository.entity.IdentificationJPA;
import org.recolnat.collection.manager.repository.entity.LocationJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.entity.TaxonJPA;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.repository.jpa.SpecimenUpdateJPARepository;
import org.recolnat.collection.manager.service.imports.ImportSpecimenProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("int")
@Slf4j
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ImportSpecimenProcessorITest extends AbstractResourceDBTest {

    @Autowired
    private ImportSpecimenProcessor importSpecimenProcessor;

    @Autowired
    private SpecimenJPARepository specimenJPARepository;

    @Autowired
    private SpecimenUpdateJPARepository specimenUpdateJPARepository;

    private static void checkUpdatedSpecimen(SpecimenJPA specimen) {
        assertThat(specimen.getRecordNumber()).isEqualTo("502");
        //  TODO DTH vérifier la totalité des champs
    }

    private static void checkInsertedSpecimen(SpecimenJPA specimen) {
        // specimen
        assertThat(specimen.getCollectionCode()).isEqualTo("UCBL-FSL");
        assertThat(specimen.getCatalogNumber()).isEqualTo("UCBL-FSL 15234");
        assertThat(specimen.getNominativeCollection()).isEqualTo("Collection nominale");
        assertThat(specimen.getRecordNumber()).isEqualTo("302");
        assertThat(specimen.getBasisOfRecord()).isEqualTo("PreservedSpecimen");
        assertThat(specimen.getPreparations()).isEqualTo("tube");
        assertThat(specimen.getSex()).isEqualTo("male");
        assertThat(specimen.getLifeStage()).isEqualTo("juvenile");
        assertThat(specimen.getIndividualCount()).isEqualTo("1");
        assertThat(specimen.getOccurrenceRemarks()).isEqualTo("prété");
        assertThat(specimen.getLegalStatus()).isEqualTo("Collection privée");
        assertThat(specimen.getDonor()).isEqualTo("Jean");
        assertThat(specimen.getMids()).isEqualTo(1);

        // collection
        assertThat(specimen.getCollection().getCollectionNameFr()).isEqualTo("UCB Lyon 1");

        // collection_event
        CollectionEventJPA collectionEvent = specimen.getCollectionEvent();
        assertThat(collectionEvent.getEventDate()).isEqualTo("1984-11-10/2020-05-19");
        assertThat(collectionEvent.getInterpretedDate()).isFalse();
        assertThat(collectionEvent.getRecordedBy()).isEqualTo("paul henri");
        assertThat(collectionEvent.getFieldNumber()).isEqualTo("ph021");
        assertThat(collectionEvent.getFieldNotes()).isEqualTo("terrain humide");
        assertThat(collectionEvent.getEventRemarks()).isEqualTo("piège");
        assertThat(collectionEvent.getVerbatimLocality()).isEqualTo("A 20 pieds de profondeur dans la mer, en face de Mavrospilios.");
        assertThat(collectionEvent.getSensitiveLocation()).isFalse();
        assertThat(collectionEvent.getDecimalLatitude()).isEqualTo(45.808408);
        assertThat(collectionEvent.getDecimalLongitude()).isEqualTo(5.790681);
        assertThat(collectionEvent.getGeodeticDatum()).isEqualTo("WGS84");
        assertThat(collectionEvent.getGeoreferenceSources()).isEqualTo("atlas");
        assertThat(collectionEvent.getMinimumElevationInMeters()).isEqualTo(101.21);
        assertThat(collectionEvent.getMaximumElevationInMeters()).isEqualTo(205.54);
        assertThat(collectionEvent.getInterpretedAltitude()).isFalse();
        assertThat(collectionEvent.getMinimumDepthInMeters()).isEqualTo(150);
        assertThat(collectionEvent.getMaximumDepthInMeters()).isEqualTo(200);
        assertThat(collectionEvent.getInterpretedDepth()).isFalse();
        assertThat(collectionEvent.getHabitat()).isEqualTo("Arbre");

        LocationJPA location = collectionEvent.getLocation();
        assertThat(location.getLocality()).isEqualTo("Sans localité précise");
        assertThat(location.getMunicipality()).isEqualTo("Chanaz");
        assertThat(location.getCounty()).isEqualTo("Savoie");
        assertThat(location.getRegion()).isEqualTo("Ile-de-France");
        assertThat(location.getStateProvince()).isEqualTo("Auvergne-Rhône-Alpes");
        assertThat(location.getCountry()).isEqualTo("france");
        assertThat(location.getCountryCode()).isEqualTo("FR");
        assertThat(location.getContinent()).isEqualTo("Europe");
        assertThat(location.getIsland()).isEqualTo("Une ile");
        assertThat(location.getIslandGroup()).isEqualTo("Un groupe d'iles");
        assertThat(location.getWaterBody()).isEqualTo("Une masse d'eau");
        assertThat(location.getLocationRemarks()).isEqualTo("Localité difficile à interpréter");

        // identification
        assertThat(specimen.getIdentifications()).hasSize(1);
        IdentificationJPA actual = specimen.getIdentifications().stream().toList().get(0);
        assertThat(actual.getCurrentDetermination()).isTrue();
        assertThat(actual.getVerbatimIdentification()).isEqualTo("Hecticoceras zieteni");
        assertThat(actual.getIdentificationVerificationStatus()).isFalse();
        assertThat(actual.getTypeStatus()).isEqualTo("Holotype");
        assertThat(actual.getIdentifiedByID()).isEqualTo("Jacques Franklin");
        assertThat(actual.getDateIdentified()).isEqualTo(LocalDate.of(2020, 1, 1));
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
        assertThat(taxon.getTaxonRemarks()).isEqualTo("Remarques taxon");

        // geological_context
        GeologicalContextJPA geologicalContext = specimen.getGeologicalContext();
        assertThat(geologicalContext).isNotNull();
        assertThat(geologicalContext.getVerbatimEpoch()).isEqualTo("Lutétien supérieur");
        assertThat(geologicalContext.getAgeAbsolute()).isEqualTo("age absolu");
        assertThat(geologicalContext.getEarliestAgeOrLowestStage()).isEqualTo("Callovien");
        assertThat(geologicalContext.getLatestAgeOrHighestStage()).isEqualTo("Frasnien");
        assertThat(geologicalContext.getEarliestEpochOrLowestSeries()).isEqualTo("Jurassique moyen");
        assertThat(geologicalContext.getLatestEpochOrHighestSeries()).isEqualTo("Dévonien supérieur");
        assertThat(geologicalContext.getEarliestPeriodOrLowestSystem()).isEqualTo("Jurassique");
        assertThat(geologicalContext.getLatestPeriodOrHighestSystem()).isEqualTo("Quaternaire");
        assertThat(geologicalContext.getEarliestEraOrLowestErathem()).isEqualTo("M√©sozo√Øque");
        assertThat(geologicalContext.getLatestEraOrHighestErathem()).isEqualTo("Lorem");
        assertThat(geologicalContext.getEarliestEonOrLowestEonothem()).isEqualTo("Phan√©rozo√Øque");
        assertThat(geologicalContext.getLatestEonOrHighestEonothem()).isEqualTo("Lorem");
        assertThat(geologicalContext.getLowestBiostratigraphicZone()).isEqualTo("oxynotum");
        assertThat(geologicalContext.getHighestBiostratigraphicZone()).isEqualTo("dentatus");
        assertThat(geologicalContext.getGroup()).isEqualTo("Unité 8");
        assertThat(geologicalContext.getFormation()).isEqualTo("Marnes d'Agoudim (a)");
        assertThat(geologicalContext.getMember()).isEqualTo("Pech Saint-Sauveur");
        assertThat(geologicalContext.getBed()).isEqualTo("92.3");
    }

    @Transactional
    @Test
    @Sql(scripts = "classpath:import/data/handleImport.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    void add_specimen_full_fields() throws Exception {
        String filePath = "import/specimen/data/specimen_full_fields.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);
        String username = "John Doe";
        ImportModeEnum mode = ImportModeEnum.REPLACE;
        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");
        Map<UUID, Map<String, UUID>> collectionCache = new HashMap<>();
        collectionCache.put(institutionId, new HashMap<>());

        List<SpecimenJPA> specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).isEmpty();

        importSpecimenProcessor.handleFile(file, username, collectionCache, institutionId, mode, UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));

        specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var specimen = specimens.get(0);

        checkInsertedSpecimen(specimen);
        var updates = specimenUpdateJPARepository.findAllByImportJPA_Id(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));
        assertThat(updates).hasSize(1);
        assertThat(updates.get(0).getMode()).isEqualTo(SpecimenUpdateModeEnum.CREATED);
    }

    @Transactional
    @Test
    @Sql(scripts = "classpath:import/data/handleImport.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    void add_specimen_required_values() throws Exception {
        String filePath = "import/specimen/data/only-required-fields.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);
        String username = "John Doe";
        ImportModeEnum mode = ImportModeEnum.REPLACE;
        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");
        Map<UUID, Map<String, UUID>> collectionCache = new HashMap<>();
        collectionCache.put(institutionId, new HashMap<>());

        List<SpecimenJPA> specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).isEmpty();

        importSpecimenProcessor.handleFile(file, username, collectionCache, institutionId, mode, UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));

        specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var specimen = specimens.get(0);

        assertThat(specimen.getCollectionCode()).isEqualTo("UCBL-FSL");
        assertThat(specimen.getCatalogNumber()).isEqualTo("UCBL-FSL 15234");

        // identification
        assertThat(specimen.getIdentifications()).hasSize(1);
        IdentificationJPA actual = specimen.getIdentifications().stream().toList().get(0);
        assertThat(actual.getTaxon()).hasSize(1);

        // taxon
        TaxonJPA taxon = actual.getTaxon().get(0);
        assertThat(taxon.getLevelType()).isEqualTo(LevelTypeEnum.MASTER);
        assertThat(taxon.getScientificName()).isEqualTo("Hecticoceras zieteni");

        var updates = specimenUpdateJPARepository.findAllByImportJPA_Id(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));
        assertThat(updates).hasSize(1);
        assertThat(updates.get(0).getMode()).isEqualTo(SpecimenUpdateModeEnum.CREATED);
    }

    @Transactional
    @Test
    @Sql(scripts = "classpath:import/data/handleImport.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    void add_specimen_required_columns() throws Exception {
        String filePath = "import/specimen/data/only-required-columns.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);
        String username = "John Doe";
        ImportModeEnum mode = ImportModeEnum.REPLACE;
        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");
        Map<UUID, Map<String, UUID>> collectionCache = new HashMap<>();
        collectionCache.put(institutionId, new HashMap<>());

        List<SpecimenJPA> specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).isEmpty();

        importSpecimenProcessor.handleFile(file, username, collectionCache, institutionId, mode, UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));

        specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var specimen = specimens.get(0);

        assertThat(specimen.getCollectionCode()).isEqualTo("UCBL-FSL");
        assertThat(specimen.getCatalogNumber()).isEqualTo("UCBL-FSL 15234");

        // identification
        assertThat(specimen.getIdentifications()).hasSize(1);
        IdentificationJPA actual = specimen.getIdentifications().stream().toList().get(0);
        assertThat(actual.getTaxon()).hasSize(1);

        // taxon
        TaxonJPA taxon = actual.getTaxon().get(0);
        assertThat(taxon.getLevelType()).isEqualTo(LevelTypeEnum.MASTER);
        assertThat(taxon.getScientificName()).isEqualTo("Hecticoceras zieteni");

        var updates = specimenUpdateJPARepository.findAllByImportJPA_Id(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));
        assertThat(updates).hasSize(1);
        assertThat(updates.get(0).getMode()).isEqualTo(SpecimenUpdateModeEnum.CREATED);
    }

    @Disabled("A lancer pour des benchs pas en CI")
    @Test
    @Sql(scripts = "classpath:import/data/handleImport.sql")
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    void add_specimen_20000() throws Exception {
        String filePath = "import/data/Tests_20000_spe_incomplet.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);
        String username = "John Doe";
        ImportModeEnum mode = ImportModeEnum.REPLACE;
        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");
        Map<UUID, Map<String, UUID>> collectionCache = new HashMap<>();
        collectionCache.put(institutionId, new HashMap<>());

        var count = specimenJPARepository.count();
        assertThat(count).isEqualTo(0);

        importSpecimenProcessor.handleFile(file, username, collectionCache, institutionId, mode, UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));
        count = specimenJPARepository.count();
        assertThat(count).isEqualTo(20000);

        var updates = specimenUpdateJPARepository.findAllByImportJPA_Id(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));
        assertThat(updates).hasSize(20000);
    }

    @Sql(scripts = {"classpath:import/data/before_update.sql", "classpath:import/data/handleImport.sql"})
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Test
    void update_specimen() throws Exception {
        String filePath = "import/data/update.csv";
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);
        String username = "John Doe";
        ImportModeEnum mode = ImportModeEnum.REPLACE;
        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");
        Map<UUID, Map<String, UUID>> collectionCache = new HashMap<>();
        collectionCache.put(institutionId, new HashMap<>());

        var count = specimenJPARepository.count();
        assertThat(count).isEqualTo(1);

        importSpecimenProcessor.handleFile(file, username, collectionCache, institutionId, mode, UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));

        var specimens = specimenJPARepository.findSpecimens(institutionId, "UCB Lyon 1", "UCBL-FSL 15234");
        assertThat(specimens).hasSize(1);
        var specimen = specimens.get(0);

        checkUpdatedSpecimen(specimen);

        var updates = specimenUpdateJPARepository.findAllByImportJPA_Id(UUID.fromString("c29b7295-8ea0-4a87-9714-68164ddf7abc"));
        assertThat(updates).hasSize(1);
        assertThat(updates.get(0).getMode()).isEqualTo(SpecimenUpdateModeEnum.UPDATED);
    }
}
