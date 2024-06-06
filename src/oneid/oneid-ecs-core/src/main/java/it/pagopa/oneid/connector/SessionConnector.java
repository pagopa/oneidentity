package it.pagopa.oneid.connector;

import it.pagopa.oneid.model.session.Session;

import java.util.Optional;

public interface SessionConnector<T extends Session> {

    // TODO - remove comment ideas:
    //  1. Filters to use during update SAMLSession: id, empty SAMLResponse
    //  2. Check if token already exists when creating AccessTokenSession, if exists returns same token
    void saveSession(T session);

    // TODO - remove comment - id is: (find a better name)
    //   SAMLRequestID for SAMLSession;
    //  authorizationCode for OIDCSession;
    //  accessToken for AccessTokenSession;
    Optional<T> findSession(String identifier);


    void updateSAMLSession(String samlRequestID, String SAMLResponse);


}
