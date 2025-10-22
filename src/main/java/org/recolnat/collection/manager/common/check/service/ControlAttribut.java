package org.recolnat.collection.manager.common.check.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;


@Component
@RequiredArgsConstructor
@Slf4j
public class ControlAttribut {

    public static final String NOT_FOUND = " not found";
    public static final String COLLECTION_WITH_ID = "Collection with id";
    public static final String ROLE_NOT_SUPPORTED = "Role not supported: ";

    public static final String THIS_COLLECTION = "This collection:";


    private final CollectionJPARepository collectionJPARepository;

    private final AuthenticationService authenticationService;

    private static boolean isInstitutionMember(CollectionJPA colJPA, UserAttributes currentUser) {
        final var institution = currentUser.getInstitution();
        return nonNull(institution) && institution.equals(colJPA.getInstitutionId());
    }

    private static void checkInstitutionMember(CollectionJPA colJPA, UserAttributes currentUser) {
        if (!isInstitutionMember(colJPA, currentUser)) {
            log.info("collection : {}", colJPA);
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                    THIS_COLLECTION + " " + colJPA.getId() + " does not belong to the institution: " + currentUser.getInstitution());
        }
    }

    public void checkCollectionUpdate(CollectionJPA colJPA, SpecimenJPA specJPA) {
        // Si aucun changement de collection
        if (colJPA.getId().equals(specJPA.getCollection().getId())) {
            return;
        }

        var currentUser = authenticationService.findUserAttributes();
        var role = RoleEnum.fromValue(currentUser.getRole());
        switch (role) {
            case ADMIN -> {
                // L'administrateur a les droits sur toutes les collections
            }
            case ADMIN_INSTITUTION -> {
                if (!isInstitutionMember(colJPA, currentUser)) {
                    checkInstitutionMember(colJPA, currentUser);
                }
            }
            case ADMIN_COLLECTION -> {
                var userCollections = Optional.ofNullable(currentUser.getCollections()).orElse(Collections.emptyList());
                if (!userCollections.contains(colJPA.getId())) {
                    var exception = new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                            "Imcompatible data : you cannot access this collection " + THIS_COLLECTION);

                    log.error(exception.getMessage(), exception);
                    throw exception;
                }

            }
            default ->
                    throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ROLE_NOT_SUPPORTED + currentUser.getRole());
        }
    }

    public void checkSpecimenCreateOrUpdateAsPublished(UUID collectionId) {
        var colJPA = collectionJPARepository.findById(collectionId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        COLLECTION_WITH_ID + " :" + collectionId + NOT_FOUND));

        var currentUser = authenticationService.findUserAttributes();

        switch (RoleEnum.fromValue(currentUser.getRole())) {
            case ADMIN -> {
                // L'administrateur a les droits sur toutes les collections
            }
            case ADMIN_INSTITUTION -> checkInstitutionMember(colJPA, currentUser);

            case ADMIN_COLLECTION -> {
                var allColByInst = collectionJPARepository.findCollectionsByInstitutionId(currentUser.getInstitution()).stream().map(CollectionJPA::getId)
                        .toList();
                var userCollections = Optional.ofNullable(currentUser.getCollections()).stream().flatMap(Collection::stream).toList();

                if (!(isInstitutionMember(colJPA, currentUser)
                      && (allColByInst.contains(collectionId)) && !CollectionUtils.isEmpty(currentUser.getCollections()) && new HashSet<>(allColByInst).containsAll(currentUser.getCollections()))) {
                    var exception = new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                            "Imcompatible data : your assigned collections: " + userCollections + " all collection for your institution: " + allColByInst
                            + " you cannot request" + THIS_COLLECTION + " collectionId");

                    log.error(exception.getMessage(), exception);
                    throw exception;
                }
            }
            default ->
                    throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ROLE_NOT_SUPPORTED + currentUser.getRole());
        }
    }

    /**
     * Vérifie que l'utilisateur courant a les droits sur la collection en paramètre. Jette une exception le cas échéant.
     *
     * @param colJPA collection à vérifier
     */
    public void checkUserRightsOnCollection(CollectionJPA colJPA) {
        var currentUser = authenticationService.findUserAttributes();

        switch (RoleEnum.fromValue(currentUser.getRole())) {
            case ADMIN -> {
                // L'administrateur a les droits sur toutes les collections
            }
            case ADMIN_INSTITUTION -> checkInstitutionMember(colJPA, currentUser);

            case ADMIN_COLLECTION, DATA_ENTRY -> {
                var userCollections = Optional.ofNullable(currentUser.getCollections()).stream().flatMap(Collection::stream).toList();

                if (!(isInstitutionMember(colJPA, currentUser) && userCollections.contains(colJPA.getId()))) {
                    var exception = new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                            "You cannot request" + THIS_COLLECTION + " collectionId");

                    log.error(exception.getMessage(), exception);
                    throw exception;
                }
            }
            default ->
                    throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ROLE_NOT_SUPPORTED + currentUser.getRole());
        }
    }

    public void checkUserRightsOnCollectionByCollectionName(String collectionName, UUID institutionId) {
        var colJPA = collectionJPARepository.findByCollectionNameFrAndInstitution_InstitutionId(collectionName, institutionId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        COLLECTION_WITH_ID + " :" + collectionName + NOT_FOUND));
        checkUserRightsOnCollection(colJPA);
    }

    /**
     * Vérifie que l'utilisateur courant a les droits sur la collection en paramètre. Jette une exception le cas échéant.
     *
     * @param collectionId identifiant de la collection à vérifier
     */
    public void checkUserRightsOnCollection(UUID collectionId) {
        var colJPA = collectionJPARepository.findById(collectionId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        COLLECTION_WITH_ID + " :" + collectionId + NOT_FOUND));
        checkUserRightsOnCollection(colJPA);
    }

    /**
     * Vérification du droit de l'utilisateur sur les fonctionnalités d'administration d'institution sur une collection
     *
     * @param collectionId identifiant de la collection
     */
    public void checkUserAuthAttributesForRoleAdminInst(UUID collectionId) {
        var currentUser = authenticationService.findUserAttributes();

        var colJPA = collectionJPARepository.findById(collectionId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        COLLECTION_WITH_ID + " :" + collectionId + NOT_FOUND));

        // L'administrateur est autorisé sur toutes les collections
        if (RoleEnum.ADMIN.equals(RoleEnum.fromValue(currentUser.getRole()))) {
            return;
        }

        if (RoleEnum.ADMIN_INSTITUTION.equals(RoleEnum.fromValue(currentUser.getRole()))) {
            if (!isInstitutionMember(colJPA, currentUser)) {
                checkInstitutionMember(colJPA, currentUser);
            }
        } else {
            final var exception = new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                    ROLE_NOT_SUPPORTED + currentUser.getRole());
            log.error(exception.getMessage(), exception);
            throw exception;
        }
    }

}

