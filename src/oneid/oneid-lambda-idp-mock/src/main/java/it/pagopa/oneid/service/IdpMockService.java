package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;

public interface IdpMockService {

  Response createSamlResponse(AuthnRequest authnRequest) throws SAMLUtilsException;
}
