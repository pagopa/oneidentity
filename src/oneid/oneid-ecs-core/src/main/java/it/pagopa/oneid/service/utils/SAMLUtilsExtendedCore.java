package it.pagopa.oneid.service.utils;


import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.SERVICE_PROVIDER_URI;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.model.dto.AttributeDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
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
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

@ApplicationScoped
public class SAMLUtilsExtendedCore extends SAMLUtils {

  private final MetadataCredentialResolver metadataCredentialResolver;
  private final FilesystemMetadataResolver metadataResolver;

  @Inject
  SAMLUtilsConstants samlUtilsConstants;

  @Inject
  public SAMLUtilsExtendedCore() throws SAMLUtilsException {
    super();
    //TODO: add CIE metadata resolver
    // TODO: env var fileName (DEV, UAT, PROD)
    String fileName = "metadata/spid.xml";

    try {
      metadataResolver = new FilesystemMetadataResolver(new File(fileName));
    } catch (ResolverException e) {
      throw new SAMLUtilsException(e);
    }

    metadataResolver.setId("spidMetadataResolver");
    metadataResolver.setParserPool(basicParserPool);
    try {
      metadataResolver.initialize();
    } catch (ComponentInitializationException e) {
      throw new SAMLUtilsException(e);
    }

    metadataCredentialResolver = new MetadataCredentialResolver();
    PredicateRoleDescriptorResolver roleResolver = new PredicateRoleDescriptorResolver(
        metadataResolver);
    KeyInfoCredentialResolver keyResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
    metadataCredentialResolver.setKeyInfoCredentialResolver(keyResolver);
    metadataCredentialResolver.setRoleDescriptorResolver(roleResolver);

    try {
      metadataCredentialResolver.initialize();
      roleResolver.initialize();
    } catch (ComponentInitializationException e) {
      throw new SAMLUtilsException(e);
    }
  }

  public static Issuer buildIssuer() {
    Issuer issuer = buildSAMLObject(Issuer.class);
    issuer.setValue(SERVICE_PROVIDER_URI);
    issuer.setNameQualifier(SERVICE_PROVIDER_URI);
    issuer.setFormat(NameIDType.ENTITY);

    return issuer;
  }

  public static NameIDPolicy buildNameIdPolicy() {
    NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);
    nameIDPolicy.setFormat(NameIDType.TRANSIENT);

    return nameIDPolicy;
  }

  public static RequestedAuthnContext buildRequestedAuthnContext(String spidLevel) {
    RequestedAuthnContext requestedAuthnContext = buildSAMLObject(RequestedAuthnContext.class);
    requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
    requestedAuthnContext.getAuthnContextClassRefs().add(buildAuthnContextClassRef(spidLevel));

    return requestedAuthnContext;
  }

  private static AuthnContextClassRef buildAuthnContextClassRef(String spidLevel) {
    AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
    authnContextClassRef.setURI(spidLevel);

    return authnContextClassRef;
  }

  private static String getAttributeValue(XMLObject attributeValue) {
    Log.debug("[SAMLUtilsExtendedCore.getAttributeValue] start");

    return attributeValue == null ?
        null :
        attributeValue instanceof XSString ?
            getStringAttributeValue((XSString) attributeValue) :
            attributeValue instanceof XSAnyImpl ?
                getAnyAttributeValue((XSAnyImpl) attributeValue) :
                attributeValue.toString();
  }

  private static String getStringAttributeValue(XSString attributeValue) {
    return attributeValue.getValue();
  }

  private static String getAnyAttributeValue(XSAnyImpl attributeValue) {
    return attributeValue.getTextContent();
  }

  public static Optional<List<AttributeDTO>> getAttributeDTOListFromAssertion(Assertion assertion) {
    Log.debug("[SAMLUtilsExtendedCore.getAttributeDTOListFromAssertion] start");
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
    Log.debug("[SAMLUtilsExtendedCore.getAttributeDTOListFromAssertion] end");
    return Optional.of(attributes);
  }

  public Response getSAMLResponseFromString(String SAMLResponse)
      throws OneIdentityException {
    Log.debug("[it.pagopa.oneid.common.utils.SAMLUtils.getSAMLResponseFromString] start");

    byte[] decodedSamlResponse = Base64.decodeBase64(SAMLResponse);

    try {
      return (Response) XMLObjectSupport.unmarshallFromInputStream(basicParserPool,
          new ByteArrayInputStream(decodedSamlResponse));
    } catch (XMLParserException | UnmarshallingException e) {
      Log.error(
          "[it.pagopa.oneid.common.utils.SAMLUtils.getSAMLResponseFromString] Error unmarshalling "
              + e.getMessage());
      throw new OneIdentityException(e);
    }
  }

  public void validateSignature(Response response, String entityID) throws SAMLUtilsException {
    Log.debug("[it.pagopa.oneid.common.utils.SAMLUtils.validateSignature] start");
    SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
    Assertion assertion = response.getAssertions().getFirst();

    Optional<Credential> credential = getCredential(entityID);

    if (credential.isPresent()) {
      // Validate 'Response' signature
      if (response.getSignature() != null) {
        try {
          profileValidator.validate(response.getSignature());
          SignatureValidator.validate(response.getSignature(), credential.get());
        } catch (SignatureException e) {
          Log.error(
              "[it.pagopa.oneid.common.utils.SAMLUtils.validateSignature] Error during Response signature validation "
                  + e.getMessage());
          throw new SAMLUtilsException(e);
        }
      }
      // Validate 'Assertion' signature
      if (assertion.getSignature() != null) {
        try {
          profileValidator.validate(assertion.getSignature());
          SignatureValidator.validate(assertion.getSignature(), credential.get());
        } catch (SignatureException e) {
          Log.error(
              "[it.pagopa.oneid.common.utils.SAMLUtils.getSAMLResponseFromString] Error during Assertion signature validation "
                  + e.getMessage());
          throw new SAMLUtilsException(e);
        }
      }
    } else {
      Log.error(
          "[it.pagopa.oneid.common.utils.SAMLUtils.getSAMLResponseFromString] credential not found for selected IDP "
              + assertion.getIssuer().getValue());
      throw new SAMLUtilsException("Credential not found for selected IDP");
    }
  }

  public Optional<Credential> getCredential(String entityID) throws SAMLUtilsException {
    // TODO do we need to add a cache layer? (With ConcurrentHashMap)
    CriteriaSet criteriaSet = new CriteriaSet();
    criteriaSet.add(new EntityIdCriterion(entityID));
    criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
    criteriaSet.add(new EntityRoleCriterion(
        new QName("urn:oasis:names:tc:SAML:2.0:metadata", "IDPSSODescriptor", "md")));
    criteriaSet.add(new ProtocolCriterion(SAMLConstants.SAML20P_NS));

    Credential credential;
    try {
      credential = metadataCredentialResolver.resolveSingle(criteriaSet);
    } catch (ResolverException e) {
      Log.error(
          "[it.pagopa.oneid.common.utils.SAMLUtils.getCredential] Error during credential resolving "
              + e.getMessage());
      throw new SAMLUtilsException(e);
    }

    return Optional.ofNullable(credential);
  }

  public Optional<EntityDescriptor> getEntityDescriptor(String entityID) throws SAMLUtilsException {
    // TODO do we need to add a cache layer? (With ConcurrentHashMap)
    CriteriaSet criteriaSet = new CriteriaSet();
    criteriaSet.add(new EntityIdCriterion(entityID));

    EntityDescriptor entityDescriptor;
    try {
      entityDescriptor = metadataResolver.resolveSingle(criteriaSet);
    } catch (ResolverException e) {
      throw new SAMLUtilsException(e);
    }

    return Optional.ofNullable(entityDescriptor);
  }

  public Optional<String> buildDestination(String idpID) throws SAMLUtilsException {

    return getEntityDescriptor(idpID)
        .map(descriptor -> descriptor.getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol")
            .getSingleSignOnServices()
            .getFirst()
            .getLocation()
        );
  }
}
