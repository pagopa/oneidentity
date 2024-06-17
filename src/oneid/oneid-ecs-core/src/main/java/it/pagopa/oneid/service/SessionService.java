package it.pagopa.oneid.service;

import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.Session;
import org.opensaml.saml.saml2.core.Response;

import java.util.Optional;

public interface SessionService<T extends Session> {

    void saveSession(T session) throws SessionException;

    Optional<T> getSession(String id);

    void setSAMLResponse(String samlRequestID, Response response);
}
