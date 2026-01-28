package org.recolnat.collection.manager.web;

import io.recolnat.api.RetrieveUsersApi;
import io.recolnat.model.UserDashboardPageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.connector.api.AuthorisationConnector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class UserResource implements RetrieveUsersApi {
    private final AuthorisationConnector authorisationConnector;

    @Override
    public ResponseEntity<UserDashboardPageResponseDTO> getUsers(UUID institutionId, Integer page, Integer size, String searchTerm) {
        try {
            var result = authorisationConnector.getUsers(institutionId, page, size, searchTerm);
            return new ResponseEntity<>(result, OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }
}
