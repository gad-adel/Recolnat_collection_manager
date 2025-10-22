package org.recolnat.collection.manager.specimen.api.domain.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recolnat.collection.manager.api.domain.CollectionCreate;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.mapper.CollectionMapper;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.entity.InstitutionJPA;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.recolnat.collection.manager.repository.jpa.InstitutionRepositoryJPA;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.recolnat.collection.manager.service.AuthorisationService;
import org.recolnat.collection.manager.service.impl.CollectionIntegrationServiceImpl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Disabled("Unnecessary stubbings detected.\n" +
          "Clean & maintainable test code requires zero unnecessary code.\n" +
          "Following stubbings are unnecessary (click to navigate to relevant line of code):")
class CollectionIntegrationServiceImplTest {

    @InjectMocks
    private CollectionIntegrationServiceImpl collectionIntegrationServiceImpl;
    @Mock
    private Validator validator;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CollectionMapper collectionMapper;
    @Mock
    private CollectionJPARepository collectionJPARepository;
    @Mock
    private InstitutionRepositoryJPA institutionRepositoryJPA;
    @Mock
    private ConstraintViolation constraintViolation;

    @Test
    @DisplayName("given Collection when Add then Return Collection Save")
    void givenCollection_whenAdd_thenReturnCollectionSave() {
        // Given - precondition or setup
        var collection = CollectionCreate.builder()
                .domain("test_type")
                .collectionNameFr("test_name")
                .institutionId(new UUID(0, 1))
                .build();
        Set<ConstraintViolation<CollectionCreate>> validate = new HashSet<>();
        UserAttributes user = UserAttributes.builder()
                .institution(1).build();

        var collectionJPA = CollectionJPA.builder()
                .typeCollection("test_type")
                .collectionNameFr("test_name")
                .institutionId(1)
                .build();
        var uuidCol = "cace37b1-5eaa-41a8-83f8-745e088665e3";
        var collectionJPASave = CollectionJPA.builder()
                .typeCollection("test_type")
                .collectionNameFr("test_name")
                .institutionId(1)
                .id(UUID.fromString(uuidCol)).build();
        // When - action or the behaviour
        when(validator.validate(collection)).thenReturn(validate);
        InstitutionJPA institution = InstitutionJPA.builder().id(1).institutionId(new UUID(0, 1)).build();
        when(institutionRepositoryJPA.findInstitutionByInstitutionId(new UUID(0, 1))).thenReturn(Optional.ofNullable(institution));
        when(collectionMapper.collectionTocollectionJPA(any())).thenReturn(collectionJPA);
        when(collectionJPARepository.save(any())).thenReturn(collectionJPASave);
        UUID addCollection = collectionIntegrationServiceImpl.addCollection(collection);
        // Then - verify the output
        assertThat(addCollection).isNotNull();
    }

    @Disabled("Unnecessary stubbings detected.\n" +
              "Clean & maintainable test code requires zero unnecessary code.\n" +
              "Following stubbings are unnecessary (click to navigate to relevant line of code):")
    @Test
    void givenCollection_whenInstitNoValid_thenReturnException() {
        // Given - precondition or setup
        var collection = CollectionCreate.builder()
                .domain("test_type")
                .collectionNameFr("test_name")
                .institutionId(new UUID(0, 1))
                .build();
        Set<ConstraintViolation<CollectionCreate>> validate = new HashSet<>();
        // When - action or the behaviour
        validate.add(constraintViolation);
        when(validator.validate(collection)).thenReturn(validate);
        CollectionManagerBusinessException assertThrows = assertThrows(CollectionManagerBusinessException.class, () ->
                collectionIntegrationServiceImpl.addCollection(collection));
//		 Then - verify the output
        assertThat(assertThrows.getCode()).isEqualTo("ERR_CODE_INVALID_REQUEST");
    }

    @Test
    @Disabled(value = "to much mocks : test CollectionIntegrationResource#add")
    void givenCollection_whenUserWithoutInstit_thenReturnThrowException() {
        // Given - precondition or setup
        var collection = CollectionCreate.builder()
                .domain("test_type")
                .collectionNameFr("test_name")
                .institutionId(new UUID(0, 1))
                .build();
        Set<ConstraintViolation<CollectionCreate>> validate = new HashSet<>();
        UserAttributes user = UserAttributes.builder()
                .role(RoleEnum.ADMIN.name()).build();
        // When - action or the behaviour
        when(validator.validate(collection)).thenReturn(validate);
        InstitutionJPA institution = InstitutionJPA.builder().id(1).institutionId(new UUID(0, 1)).build();
        when(institutionRepositoryJPA.findInstitutionByInstitutionId(new UUID(0, 1))).thenReturn(Optional.ofNullable(institution));
        when(authenticationService.findUserAttributes()).thenReturn(user);
        CollectionManagerBusinessException assertThrows = assertThrows(CollectionManagerBusinessException.class, () ->
                collectionIntegrationServiceImpl.addCollection(collection));
//		 Then - verify the output
        assertThat(assertThrows.getCode()).isEqualTo("ERR_CODE_INVALID_REQUEST");
    }

    @Test
    @Disabled(value = "to much mocks : test CollectionIntegrationResource#add")
    void givenCollection_when_NoInstitutionById_thenReturnThrowException() {
        // Given - precondition or setup
        var collection = CollectionCreate.builder()
                .domain("test_type")
                .collectionNameFr("test_name")
                .institutionId(new UUID(0, 1))
                .build();
        Set<ConstraintViolation<CollectionCreate>> validate = new HashSet<>();
        UserAttributes user = UserAttributes.builder()
                .institution(1).build();
        // When - action or the behaviour
        when(validator.validate(collection)).thenReturn(validate);
        when(authenticationService.findUserAttributes()).thenReturn(user);
        InstitutionJPA institution = InstitutionJPA.builder().id(1).institutionId(new UUID(0, 1)).build();
        when(institutionRepositoryJPA.findInstitutionByInstitutionId(new UUID(0, 1))).thenReturn(Optional.ofNullable(institution));
        CollectionManagerBusinessException assertThrows = assertThrows(CollectionManagerBusinessException.class, () ->
                collectionIntegrationServiceImpl.addCollection(collection));
//		 Then - verify the output
        assertThat(assertThrows.getCode()).isEqualTo("ERR_CODE_INVALID_REQUEST");
    }

}
