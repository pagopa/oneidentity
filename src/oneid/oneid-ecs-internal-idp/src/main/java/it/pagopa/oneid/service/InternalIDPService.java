package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import java.util.Set;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.w3c.dom.Element;

public interface InternalIDPService {

  AuthnRequest getAuthnRequestFromString(String authnRequest);

  void validateAuthnRequest(AuthnRequest authnRequest);

  Client getClientByAttributeConsumingServiceIndex(AuthnRequest authnRequest);

  Response createSuccessfulSamlResponse(String authnRequestId, String clientId, String username)
      throws SAMLUtilsException;

  Element getElementValueFromSamlResponse(Response samlResponse);

  String getStringValue(Element element);

  void validateUserInformation(String clientId, String username, String password)
      throws OneIdentityException;

  Set<String> retrieveClientRequestedParameters(String clientId);

}
