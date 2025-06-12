package it.pagopa.oneid.service;

import it.pagopa.oneid.model.IDPSession;

public interface SessionService {

  IDPSession validateAuthnRequestIdCookie(String authnRequestId, String username);


}
