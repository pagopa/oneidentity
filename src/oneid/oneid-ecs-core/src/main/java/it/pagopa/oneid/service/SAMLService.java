package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import java.util.List;
import java.util.Optional;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

public interface SAMLService {

  AuthnRequest buildAuthnRequest(String idpID, int assertionConsumerServiceIndex,
      int attributeConsumingServiceIndex, String spidLevel)
      throws OneIdentityException;

  void validateSAMLResponse(Response SAMLResponse, String entityID)
      throws OneIdentityException;

  Response getSAMLResponseFromString(String SAMLResponse) throws OneIdentityException;

  List<AttributeDTO> getAttributesFromSAMLAssertion(Assertion assertion)
      throws OneIdentityException;

  Optional<EntityDescriptor> getEntityDescriptorFromEntityID(String entityID)
      throws OneIdentityException;

  void checkSAMLStatus(Response response) throws OneIdentityException;

}
