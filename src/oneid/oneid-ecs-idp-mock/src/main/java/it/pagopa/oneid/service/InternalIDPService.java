package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import org.opensaml.saml.saml2.core.AuthnRequest;

public interface InternalIDPService {

  AuthnRequest getAuthnRequestFromString(String authnRequest) throws OneIdentityException;

  AuthnRequest validateAuthnRequest(AuthnRequest authnRequest) throws OneIdentityException;

}
