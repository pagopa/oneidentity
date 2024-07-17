package it.pagopa.oneid.service.utils;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.util.Optional;
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;

@ApplicationScoped
public class MetadataResolverExtended {


  private MetadataCredentialResolver spidMetadataCredentialResolver;
  private MetadataCredentialResolver cieMetadataCredentialResolver;
  private FilesystemMetadataResolver spidMetadataResolver;
  private FilesystemMetadataResolver cieMetadataResolver;
  private BasicParserPool basicParserPool;

  @Inject
  public MetadataResolverExtended(BasicParserPool basicParserPool) throws SAMLUtilsException {
    this.basicParserPool = basicParserPool;
    metadataResolverSetter();

  }

  private void metadataResolverSetter() throws SAMLUtilsException {
    try {
      spidMetadataResolver = new FilesystemMetadataResolver(new File("metadata/spid.xml"));
      cieMetadataResolver = new FilesystemMetadataResolver(new File("metadata/cie.xml"));
    } catch (ResolverException e) {
      throw new SAMLUtilsException(e);
    }

    spidMetadataResolver.setId("spidMetadataResolver");
    spidMetadataResolver.setParserPool(basicParserPool);
    cieMetadataResolver.setId("cieMetadataResolver");
    cieMetadataResolver.setParserPool(basicParserPool);
    try {
      spidMetadataResolver.initialize();
      cieMetadataResolver.initialize();
    } catch (ComponentInitializationException e) {
      throw new SAMLUtilsException(e);
    }

    spidMetadataCredentialResolver = new MetadataCredentialResolver();
    PredicateRoleDescriptorResolver spidRoleResolver = new PredicateRoleDescriptorResolver(
        spidMetadataResolver);
    KeyInfoCredentialResolver spidKeyResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
    spidMetadataCredentialResolver.setRoleDescriptorResolver(spidRoleResolver);
    spidMetadataCredentialResolver.setKeyInfoCredentialResolver(spidKeyResolver);

    cieMetadataCredentialResolver = new MetadataCredentialResolver();
    PredicateRoleDescriptorResolver cieRoleResolver = new PredicateRoleDescriptorResolver(
        cieMetadataResolver);
    KeyInfoCredentialResolver cieKeyResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
    cieMetadataCredentialResolver.setRoleDescriptorResolver(cieRoleResolver);
    cieMetadataCredentialResolver.setKeyInfoCredentialResolver(cieKeyResolver);

    try {
      spidMetadataCredentialResolver.initialize();
      cieMetadataCredentialResolver.initialize();
      spidRoleResolver.initialize();
      cieRoleResolver.initialize();
    } catch (ComponentInitializationException e) {
      throw new SAMLUtilsException(e);
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
      if (entityID.equals(
          "https://idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO")) {
        credential = cieMetadataCredentialResolver.resolveSingle(criteriaSet);
      } else {
        credential = spidMetadataCredentialResolver.resolveSingle(criteriaSet);

      }
    } catch (ResolverException e) {
      Log.error(
          "error during credential resolving "
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
      if (entityID.equals(
          "https://idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO")) {
        entityDescriptor = cieMetadataResolver.resolveSingle(criteriaSet);
      } else {
        entityDescriptor = spidMetadataResolver.resolveSingle(criteriaSet);
      }
    } catch (ResolverException e) {
      throw new SAMLUtilsException(e);
    }

    return Optional.ofNullable(entityDescriptor);
  }
}
