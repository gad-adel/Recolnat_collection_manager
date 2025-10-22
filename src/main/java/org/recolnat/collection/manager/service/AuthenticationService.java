package org.recolnat.collection.manager.service;

import org.recolnat.collection.manager.api.domain.ConnectedUser;
import org.recolnat.collection.manager.api.domain.UserAttributes;

public interface AuthenticationService {

    String SUB = "sub";
    String USER_KEY = "uid";
    String INSTITUTION_KEY = "institution";
    String COLLECTIONS_KEY = "collections";
    String ROLE_PREFIX = "ROLE_";

    UserAttributes findUserAttributes();

    ConnectedUser getConnected();
}
