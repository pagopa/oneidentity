package it.pagopa.oneid.service;

import it.pagopa.oneid.model.AccessTokenSession;
import it.pagopa.oneid.model.OIDCSession;
import it.pagopa.oneid.model.SAMLSession;
import org.opensaml.saml.saml2.core.Response;

import java.util.UUID;

public interface SessionService {

    void persistSAMLSession(SAMLSession samlSession);

    SAMLSession getSAMLSession(UUID samlRequestID);

    void setSAMLResponse(UUID samlRequestID, Response response);

    void persistOIDCSession(OIDCSession oidcSession);

    OIDCSession getOIDCSession(String authorizationCode);

    void persistAccessTokenSession(AccessTokenSession accessTokenSession);

    AccessTokenSession getAccessTokenSession(String accessToken);
}
