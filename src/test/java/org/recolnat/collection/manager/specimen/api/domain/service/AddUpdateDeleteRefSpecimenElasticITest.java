package org.recolnat.collection.manager.specimen.api.domain.service;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.recolnat.collection.manager.api.domain.CollectionEvent;
import org.recolnat.collection.manager.api.domain.GeologicalContext;
import org.recolnat.collection.manager.api.domain.Identification;
import org.recolnat.collection.manager.api.domain.Institution;
import org.recolnat.collection.manager.api.domain.Location;
import org.recolnat.collection.manager.api.domain.Media;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.Taxon;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.LanguageEnum;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.service.impl.ElasticServiceImpl;
import org.recolnat.collection.manager.service.impl.InstitutionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.api.domain.enums.LevelTypeEnum.MASTER;


@Slf4j
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@ActiveProfiles("int")
public class AddUpdateDeleteRefSpecimenElasticITest extends AbstractResourceElasticTest {

    static UUID specimenId = UUID.fromString("72d6a65d-6972-4b7e-8739-27405ece9856");
    @Autowired
    ElasticServiceImpl elasticServiceImpl;
    @SpyBean
    InstitutionServiceImpl institutionServiceImpl;
    @Value("${index.specimen}")
    String indexSpecimen;

    /**
     * cadre test container , pas d index predefini
     */
    @Test
    @Order(1)
    void verifElasticIsFalse() {

        try {

            boolean value = elasticServiceImpl.verifyIndexExist(indexSpecimen);
            assertThat(value).isFalse();

        } catch (Exception e) {
            log.error("Error while ping ." + e.getMessage());
            fail("Error while ping ." + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void addOrUpdateSpecimenToRefElastic() {
        try {
            UserAttributes user = UserAttributes.builder()
                    .role(RoleEnum.ADMIN.name()).institution(1).build();
            when(authenticationService.findUserAttributes()).thenReturn(user);

            CollectionJPA collectionJPA = CollectionJPA.builder().id(UUID.fromString("667657a5-9751-4b15-ba5f-90e78a42f678"))
                    .institutionId(1).build();

            Institution institution = Institution.builder().id(1).name("Muséum National d'Histoire Naturelle").code("MNHN").build();
            doReturn(institution).when(institutionServiceImpl).getInstitutionById(1, LanguageEnum.FR.name());

            final var spec = Specimen.builder().id(specimenId).catalogNumber("LY0387367")
                    .collectionId(UUID.fromString("667657a5-9751-4b15-ba5f-90e78a42f678")).collectionName("Herbier Louis Corbière")
                    .identifications(Set.of(
                            Identification.builder().identifiedByID("Pacaud")
                                    .identificationRemarks("identificationRemarks-01")
                                    .taxon(List.of(Taxon.builder().id(UUID.fromString("a5b330a7-a8a7-4797-b6de-02e337aff155")).genus("Odontaspis")
                                            .scientificName("Odontaspis acutissima").levelType(MASTER)
                                            .vernacularName("requins").build()))
                                    .currentDetermination(true).build(),

                            Identification.builder()
                                    .identificationRemarks("identificationRemarks-02").identifiedByID("d'Orbigny Alcide")
                                    .taxon(List.of(Taxon.builder().id(UUID.fromString("3bb03fec-a584-4bfd-bb6d-696507c32f23"))
                                            .genus("Pseudocryphaeus")
                                            .scientificName("Pseudocryphaeus michelini").vernacularName("coquillage").levelType(MASTER).build()))
                                    .currentDetermination(false).build()))

                    .geologicalContext(GeologicalContext.builder()
                            .bed("Teddibert").build())

                    .collectionEvent(CollectionEvent.builder()
                            .location(Location.builder().continent("europe").country("france").county("ile de france").build()).build())
                    .medias(List.of(Media.builder().mediaUrl("http://mediaphoto.mnhn.fr/media/1441439763721S1JxP92vYMmLFcfK").build(), Media.builder()
                            .mediaUrl("http://mediaphoto.mnhn.fr/media/1441345617880y0kI6LQPyHTYwt3G").build()))
                    .build();

            elasticServiceImpl.addOrUpdateRefSpecimenElastic(spec, collectionJPA);

        } catch (Exception e) {
            log.error("Error while performing the operation." + e.getMessage());
            fail("Error while performing the operation." + e.getMessage());
        }

    }

    @Test
    @Order(3)
    void updateSpecimenToRefElastic() {

        try {

            UserAttributes user = UserAttributes.builder()
                    .role(RoleEnum.ADMIN.name()).institution(1).build();
            when(authenticationService.findUserAttributes()).thenReturn(user);

            CollectionJPA collectionJPA = CollectionJPA.builder().id(UUID.fromString("667657a5-9751-4b15-ba5f-90e78a42f678"))
                    .institutionId(1).build();

            Institution institution = Institution.builder().id(1).name("Muséum National d'Histoire Naturelle").code("MNHN").build();
            doReturn(institution).when(institutionServiceImpl).getInstitutionById(1, LanguageEnum.FR.name());

            final var spec = Specimen.builder().id(specimenId).catalogNumber("LY0387999")
                    .collectionId(UUID.fromString("667657a5-9751-4b15-ba5f-90e78a42f678")).collectionName("Herbier Louis Corbière")
                    .identifications(Set.of(
                            Identification.builder().identifiedByID("Pacaud")
                                    .identificationRemarks("identificationRemarks-01")
                                    .taxon(List.of(Taxon.builder().id(UUID.fromString("a5b330a7-a8a7-4797-b6de-02e337aff155")).genus("Odontaspis")
                                            .scientificName("Odontaspis acutissima").levelType(MASTER)
                                            .vernacularName("requins").build()))
                                    .currentDetermination(true).build(),

                            Identification.builder()
                                    .identificationRemarks("identificationRemarks-02").identifiedByID("d'Orbigny Alcide")
                                    .taxon(List.of(Taxon.builder().id(UUID.fromString("3bb03fec-a584-4bfd-bb6d-696507c32f23")).genus("Pseudocryphaeus")
                                            .scientificName("Pseudocryphaeus michelini").vernacularName("coquillage").levelType(MASTER).build()))
                                    .currentDetermination(false).build()))

                    .geologicalContext(GeologicalContext.builder()
                            .bed("Teddibert").build())

                    .collectionEvent(CollectionEvent.builder()
                            .location(Location.builder().continent("Africa").country("Madagascar").county("Chaînes Anosyennes").build()).build())
                    .medias(List.of(Media.builder().mediaUrl("http://mediaphoto.mnhn.fr/media/1441439763721S1JxP92vYMmLFcfK").build(), Media.builder()
                            .mediaUrl("http://mediaphoto.mnhn.fr/media/1441345617880y0kI6LQPyHTYwt3G").build()))
                    .build();

            elasticServiceImpl.addOrUpdateRefSpecimenElastic(spec, collectionJPA);

        } catch (Exception e) {
            log.error("Error while performing the operation." + e.getMessage());
            fail("Error while performing the operation." + e.getMessage());
        }

    }

    @Test
    @Order(4)
    void deleteSpecimenToRefElastic() {
        try {
            elasticServiceImpl.deleteSpecimenToRefElastic(specimenId.toString());
        } catch (Exception e) {
            log.error("Error while deleting the operation." + e.getMessage());
            fail("Error while deleting the operation." + e.getMessage());
        }
    }

    @Test
    @Order(5)
    void pingElastic() {

        try {

            boolean value = elasticServiceImpl.ping();
            assertThat(value).isTrue();

        } catch (Exception e) {
            log.error("Error while ping ." + e.getMessage());
            fail("Error while ping ." + e.getMessage());
        }
    }

    /**
     * suite a l insertion d un specimen , un index par defaut a été cree, par le client low level
     */
    @Test
    @Order(6)
    void verifElasticIstrue() {

        try {

            boolean value = elasticServiceImpl.verifyIndexExist(indexSpecimen);
            assertThat(value).isTrue();

        } catch (Exception e) {
            log.error("Error while ping ." + e.getMessage());
            fail("Error while ping ." + e.getMessage());
        }
    }
}
