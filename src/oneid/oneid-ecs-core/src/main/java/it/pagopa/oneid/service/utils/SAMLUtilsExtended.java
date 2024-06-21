package it.pagopa.oneid.service.utils;


import static it.pagopa.oneid.common.model.SAMLUtilsConstants.METADATA_URL;
import static it.pagopa.oneid.common.model.SAMLUtilsConstants.SERVICE_PROVIDER_URI;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.saml2.core.Assertion;
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
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

@ApplicationScoped
public class SAMLUtilsExtended extends SAMLUtils {

  @Inject
  public SAMLUtilsExtended() throws SAMLUtilsException {
    super();
  }

  public static Issuer buildIssuer() {
    Issuer issuer = buildSAMLObject(Issuer.class);
    issuer.setValue(METADATA_URL);
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
