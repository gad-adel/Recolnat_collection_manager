package org.recolnat.collection.manager.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.recolnat.collection.manager.api.domain.CollectionEvent;
import org.recolnat.collection.manager.api.domain.Identification;
import org.recolnat.collection.manager.api.domain.Institution;
import org.recolnat.collection.manager.api.domain.Location;
import org.recolnat.collection.manager.api.domain.Media;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.Taxon;
import org.recolnat.collection.manager.api.domain.enums.LevelTypeEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_all_specimen.sql", "classpath:init_data_all_specimen.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_all_specimen.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ElasticServiceImplITest extends AbstractResourceDBTest {

    @Autowired
    private ElasticServiceImpl elasticService;

    @Test
    void specimenIndexFromSpecimen() {
        Specimen specimen = Specimen.builder()
                .id(new UUID(0, 1))
                .collectionCode("CODE_1")
                .catalogNumber("CATALOG_1")
                .nominativeCollection("Collection nominale 1")
                .medias(
                        List.of(Media.builder().mediaUrl("http://mediaphoto.mnhn.fr/media/1441360143985SJ9QKugPxDp1GNIB").build())
                )
                .collectionEvent(
                        CollectionEvent.builder()
                                .location(
                                        Location.builder()
                                                .continent("Continent")
                                                .country("Pays")
                                                .county("Conté")
                                                .municipality("Municipalité")
                                                .locality("Localité")
                                                .region("Région")
                                                .island("Ile")
                                                .islandGroup("Groupe d'iles")
                                                .waterBody("Masse d'eau")
                                                .build()
                                )
                                .decimalLatitude(-33.91667)
                                .decimalLongitude(18.43333)
                                .fieldNumber("Numéro de collecte")
                                .eventDate("2024-07-05")
                                .build()
                )
                .identifications(Set.of(
                        Identification.builder()
                                .currentDetermination(false)
                                .typeStatus("Type statut 1")
                                .identifiedByID("Identifié par 1")
                                .taxon(
                                        List.of(
                                                Taxon.builder()
                                                        .levelType(LevelTypeEnum.SECONDARY)
                                                        .scientificName("Nom scientifique 1")
                                                        .scientificNameAuthorship("Auteur 1")
                                                        .genus("Genre 1")
                                                        .family("Famille 1")
                                                        .specificEpithet("Epithète spécifique 1")
                                                        .vernacularName("Nom vernaculaire 1")
                                                        .build(),
                                                Taxon.builder()
                                                        .levelType(LevelTypeEnum.MASTER)
                                                        .scientificName("Nom scientifique 2")
                                                        .genus("Genre 2")
                                                        .family("Famille 2")
                                                        .specificEpithet("Epithète spécifique 2")
                                                        .vernacularName("Nom vernaculaire 2")
                                                        .build()
                                        )
                                )
                                .build(),
                        Identification.builder()
                                .currentDetermination(true)
                                .typeStatus("Type statut 2")
                                .identifiedByID("Identifié par 2")
                                .taxon(List.of(Taxon.builder()
                                        .levelType(LevelTypeEnum.MASTER)
                                        .scientificName("Nom scientifique 3")
                                        .scientificNameAuthorship("Auteur 3")
                                        .genus("Genre 3")
                                        .family("Famille 3")
                                        .specificEpithet("Epithète spécifique 3")
                                        .vernacularName("Nom vernaculaire 3")
                                        .build()))
                                .build()
                ))
                .build();
        CollectionJPA collection = CollectionJPA.builder()
                .id(UUID.fromString("9a342a92-6fe8-48d3-984e-d1731c051666"))
                .collectionNameFr("Tunicier")
                .collectionNameEn("Tunicates")
                .typeCollection("Botanique")
                .institutionId(1)
                .build();

        Institution institution = Institution.builder()
                .id(1)
                .institutionId(UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"))
                .name("Muséum National d'Histoire Naturelle")
                .logoUrl("http://mediaphoto.mnhn.fr/media/1703257596436moFvSbgrT4O1sPbL")
                .build();

        var index = elasticService.specimenIndexFromSpecimen(specimen, collection, institution);

        assertThat(index).isNotNull();
        assertThat(index.getId()).isEqualTo("00000000-0000-0000-0000-000000000001");
        assertThat(index.getInstitutionId()).isEqualTo(UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"));
        assertThat(index.getInstitutionName()).isEqualTo("Muséum National d'Histoire Naturelle");
        assertThat(index.getInstitutionLogoUrl()).isEqualTo("http://mediaphoto.mnhn.fr/media/1703257596436moFvSbgrT4O1sPbL");
        assertThat(index.getCollectionId()).isEqualTo("9a342a92-6fe8-48d3-984e-d1731c051666");
        assertThat(index.getCollectionCode()).isEqualTo("CODE_1");
        assertThat(index.getCollectionNameFr()).isEqualTo("Tunicier");
        assertThat(index.getCollectionNameEn()).isEqualTo("Tunicates");
        assertThat(index.getCatalogNumber()).isEqualTo("CATALOG_1");
        assertThat(index.getNominativeCollection()).isEqualTo("Collection nominale 1");
        assertThat(index.getDomain()).isEqualTo("Botanique");
        assertThat(index.getScientificNames()).isEqualTo(new String[]{"Nom scientifique 3", "Nom scientifique 2", "Nom scientifique 1"});
        assertThat(index.getScientificNameAuthorships()).isEqualTo(new String[]{"Auteur 3", "Auteur 1"});
        assertThat(index.getGenus()).isEqualTo(new String[]{"Genre 3", "Genre 2", "Genre 1"});
        assertThat(index.getVernacularName()).isEqualTo(new String[]{"Nom vernaculaire 3", "Nom vernaculaire 2", "Nom vernaculaire 1"});
        assertThat(index.getIdentificationByIds()).isEqualTo(new String[]{"Identifié par 2", "Identifié par 1"});
        assertThat(index.getTypesStatus()).isEqualTo(new String[]{"Type statut 2", "Type statut 1"});
        assertThat(index.getContinent()).isEqualTo("Continent");
        assertThat(index.getCountry()).isEqualTo("Pays");
        assertThat(index.getMunicipality()).isEqualTo("Municipalité");
        assertThat(index.getCounty()).isEqualTo("Conté");
        assertThat(index.getWaterBody()).isEqualTo("Masse d'eau");
        assertThat(index.getIsland()).isEqualTo("Ile");
        assertThat(index.getIslandGroup()).isEqualTo("Groupe d'iles");
        assertThat(index.getRegion()).isEqualTo("Région");
        assertThat(index.getLocality()).isEqualTo("Localité");
        assertThat(index.getMediaUrl()).isEqualTo("http://mediaphoto.mnhn.fr/media/1441360143985SJ9QKugPxDp1GNIB");
        assertThat(index.getDecimalLatitude()).isEqualTo(-33.91667f);
        assertThat(index.getDecimalLongitude()).isEqualTo(18.43333f);
        assertThat(index.getFieldNumber()).isEqualTo("Numéro de collecte");
        assertThat(index.getCollectionDate()).isEqualTo("2024-07-05");
        assertThat(index.getFamily()).isEqualTo(new String[]{"Famille 3", "Famille 2", "Famille 1"});
        assertThat(index.getSpecificEpithet()).isEqualTo(new String[]{"Epithète spécifique 3", "Epithète spécifique 2", "Epithète spécifique 1"});
    }
}
