package it.pagopa.oneid.service.utils;


import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.SERVICE_PROVIDER_URI;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

@ApplicationScoped
public class SAMLUtilsExtendedCore extends SAMLUtils {

  @Inject
  SAMLUtilsConstants samlUtilsConstants;

  @Inject
  MetadataResolverExtended metadataResolverExtended;

  @Inject
  public SAMLUtilsExtendedCore(BasicParserPool basicParserPool) throws SAMLUtilsException {
    super(basicParserPool);
    // TODO: env var fileName (DEV, UAT, PROD)
  }

  public SAMLUtilsExtendedCore() {
    super();
  }

  public Issuer buildIssuer() {
    Issuer issuer = buildSAMLObject(Issuer.class);
    issuer.setValue(SERVICE_PROVIDER_URI);
    issuer.setNameQualifier(SERVICE_PROVIDER_URI);
    issuer.setFormat(NameIDType.ENTITY);

    return issuer;
  }

  public NameIDPolicy buildNameIdPolicy() {
    NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);
    nameIDPolicy.setFormat(NameIDType.TRANSIENT);

    return nameIDPolicy;
  }

  public RequestedAuthnContext buildRequestedAuthnContext(String spidLevel) {
    RequestedAuthnContext requestedAuthnContext = buildSAMLObject(RequestedAuthnContext.class);
    requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
    requestedAuthnContext.getAuthnContextClassRefs().add(buildAuthnContextClassRef(spidLevel));

    return requestedAuthnContext;
  }

  private AuthnContextClassRef buildAuthnContextClassRef(String spidLevel) {
    AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
    authnContextClassRef.setURI(spidLevel);

    return authnContextClassRef;
  }

  private String getAttributeValue(XMLObject attributeValue) {
    Log.debug("start");

    return attributeValue == null ?
        null :
        attributeValue instanceof XSString ?
            getStringAttributeValue((XSString) attributeValue) :
            attributeValue instanceof XSAnyImpl ?
                getAnyAttributeValue((XSAnyImpl) attributeValue) :
                attributeValue.toString();
  }

  private String getStringAttributeValue(XSString attributeValue) {
    return attributeValue.getValue();
  }

  private String getAnyAttributeValue(XSAnyImpl attributeValue) {
    return attributeValue.getTextContent();
  }

  public Optional<List<AttributeDTO>> getAttributeDTOListFromAssertion(Assertion assertion) {
    Log.debug("start");
    List<AttributeDTO> attributes = new ArrayList<>();
    for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
      for (Attribute attribute : attributeStatement.getAttributes()) {
        List<XMLObject> attributeValues = attribute.getAttributeValues();
        if (!attributeValues.isEmpty()) {
          attributes.add(new AttributeDTO(attribute.getName(), getAttributeValue(
              attributeValues.getFirst())));
        }
      }
    }
    Log.debug("end");
    return Optional.of(attributes);
  }

  public Response getSAMLResponseFromString(String SAMLResponse)
      throws OneIdentityException {
    Log.debug("start");

    byte[] decodedSamlResponse = Base64.decodeBase64(SAMLResponse);

    try {
      return (Response) XMLObjectSupport.unmarshallFromInputStream(basicParserPool,
          new ByteArrayInputStream(decodedSamlResponse));
    } catch (XMLParserException | UnmarshallingException e) {
      Log.error(
          "error unmarshalling "
              + e.getMessage());
      throw new OneIdentityException(e);
    }
  }

  public void validateSignature(Response response, String entityID)
      throws SAMLUtilsException, SAMLValidationException {
    Log.debug("start");
    SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
    Assertion assertion = response.getAssertions().getFirst();

    Optional<Credential> credential = metadataResolverExtended.getCredential(entityID);

    if (credential.isPresent()) {
      // Validate 'Response' signature
      if (response.getSignature() != null) {
        try {
          profileValidator.validate(response.getSignature());
          SignatureValidator.validate(response.getSignature(), credential.get());
        } catch (SignatureException e) {
          Log.error(
              "error during Response signature validation "
                  + e.getMessage());
          throw new SAMLValidationException(e);
        }
      } else {
        Log.error("Response signature not present");
        throw new SAMLValidationException();
      }
      // Validate 'Assertion' signature
      if (assertion.getSignature() != null) {
        try {
          profileValidator.validate(assertion.getSignature());
          SignatureValidator.validate(assertion.getSignature(), credential.get());
        } catch (SignatureException e) {
          Log.error(
              "error during Assertion signature validation "
                  + e.getMessage());
          throw new SAMLValidationException(e);
        }
      } else {
        Log.error("Assertion signature not present");
        throw new SAMLValidationException("Assertion signature not present");
      }
    } else {
      Log.error(
          "credential not found for selected IDP "
              + assertion.getIssuer().getValue());
      throw new SAMLValidationException("Credential not found for selected IDP");
    }
  }


  public Optional<String> buildDestination(String idpID) throws SAMLUtilsException {

    return getEntityDescriptor(idpID)
        .map(descriptor -> descriptor.getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol")
            .getSingleSignOnServices()
            .getFirst()
            .getLocation()
        );
  }

  public Optional<EntityDescriptor> getEntityDescriptor(String id) throws SAMLUtilsException {
    return metadataResolverExtended.getEntityDescriptor(id);
  }
}
