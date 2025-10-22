package org.recolnat.collection.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.OperationTypeEnum;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.recolnat.collection.manager.service.AuthorisationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorisationServiceImpl implements AuthorisationService {

    private final AuthenticationService authenticationService;

    private static boolean isInstitutionMember(int institutionId, UserAttributes currentUser) {
        return Objects.nonNull(currentUser.getInstitution()) && currentUser.getInstitution() == institutionId;
    }

    private static boolean isAdminInstitution(UserAttributes currentUser) {
        return RoleEnum.ADMIN_INSTITUTION.name().equals(currentUser.getRole());
    }

    private static void checkInstitutionMember(int instId, UserAttributes currentUser) {
        if (!isInstitutionMember(instId, currentUser)) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                    " You does not belong to the institution: " + currentUser.getInstitution());
        }
    }

    /**
     * before calling this method, the thread goes through AuthorizationFilter
     */
    @Override
    public void authorize(OperationTypeEnum addCollection, int institutionId) {
        var currentUser = authenticationService.findUserAttributes();
        if (isAdminInstitution(currentUser)) {
            checkInstitutionMember(institutionId, currentUser);
        }
    }
}
