package it.pagopa.oneid;

import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.FISCAL_CODE;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.IPA_CODE;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.LOCAL_NAME_FISCAL_CODE;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.LOCAL_NAME_IPA;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.LOCAL_NAME_MUNICIPALITY;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.LOCAL_NAME_PUBLIC;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.LOCAL_NAME_PUBLIC_SERVICE_FULL_OPERATOR;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.LOCAL_NAME_VAT_NUMBER;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.MUNICIPALITY;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.NAMESPACE_PREFIX_CIE;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.NAMESPACE_PREFIX_SPID;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.NAME_FORMAT;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_DISPLAY_NAME_XML_LANG;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_NAME_XML_LANG;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_URL_XML_LANG;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.SPID_AGGREGATOR;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.VAT_NUMBER;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import it.pagopa.oneid.enums.IdType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.xml.namespace.QName;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.Company;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml.saml2.metadata.EmailAddress;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.OrganizationName;
import org.opensaml.saml.saml2.metadata.OrganizationURL;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.ServiceDescription;
import org.opensaml.saml.saml2.metadata.ServiceName;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.security.x509.BasicX509Credential;

@ApplicationScoped
public class SAMLUtilsExtendedMetadata extends SAMLUtils {

  static {
    System.setProperty("com.sun.org.apache.xml.internal.security.ignoreLineBreaks", "true");
  }

  @ConfigProperty(name = "base_path")
  String BASE_PATH;

  @ConfigProperty(name = "acs_url")
  String ACS_URL;

  @ConfigProperty(name = "slo_url")
  String SLO_URL;

  @ConfigProperty(name = "organization_name")
  String ORGANIZATION_NAME;

  @ConfigProperty(name = "organization_display_name")
  String ORGANIZATION_DISPLAY_NAME;

  @ConfigProperty(name = "organization_url")
  String ORGANIZATION_URL;

  @ConfigProperty(name = "contact_person_company")
  String CONTACT_PERSON_COMPANY;

  @ConfigProperty(name = "contact_person_email_address")
  String CONTACT_PERSON_EMAIL_ADDRESS;

  @Inject
  public SAMLUtilsExtendedMetadata(BasicParserPool basicParserPool,
      BasicX509Credential basicX509Credential, MarshallerFactory marshallerFactory)
      throws SAMLUtilsException {
    super(basicParserPool, basicX509Credential, marshallerFactory);
    // TODO: env var fileName (DEV, UAT, PROD)
  }

  public SAMLUtilsExtendedMetadata() {
    super();
  }

  public AttributeConsumingService buildAttributeConsumingService(Client client) {
    AttributeConsumingService attributeConsumingService = buildSAMLObject(
        AttributeConsumingService.class);

    attributeConsumingService.setIndex(client.getAttributeIndex());
    attributeConsumingService.getNames().add(buildServiceName(client.getFriendlyName()));
    // TODO consider adding a Service Description field in client object
    attributeConsumingService.getDescriptions()
        .add(buildServiceDescription(client.getFriendlyName()));

    for (String requestedParameter : client.getRequestedParameters()) {
      attributeConsumingService.getRequestedAttributes()
          .add(buildRequestedAttribute(requestedParameter));
    }

    return attributeConsumingService;
  }

  public ServiceName buildServiceName(String name) {
    ServiceName serviceName = buildSAMLObject(ServiceName.class);
    serviceName.setValue(name);
    // TODO consider adding a field for xml lang on service name
    serviceName.setXMLLang("it");

    return serviceName;
  }

  public ServiceDescription buildServiceDescription(String description) {
    ServiceDescription serviceDescription = buildSAMLObject(ServiceDescription.class);
    serviceDescription.setValue(description);
    // TODO consider adding a field for xml lang on service description
    serviceDescription.setXMLLang("it");

    return serviceDescription;
  }

  public RequestedAttribute buildRequestedAttribute(String requestedParameter) {
    RequestedAttribute attribute = buildSAMLObject(RequestedAttribute.class);
    attribute.setNameFormat(NAME_FORMAT);
    attribute.setName(Identifier.valueOf(requestedParameter).name());
    attribute.setFriendlyName(Identifier.valueOf(requestedParameter).getFriendlyName());

    return attribute;
  }

  public Extensions buildExtensions(String namespacePrefix, String namespaceUri) {
    Extensions extensions = buildSAMLObject(Extensions.class);

    XSAny vatNumber = new XSAnyBuilder().buildObject(namespaceUri, LOCAL_NAME_VAT_NUMBER,
        namespacePrefix);
    vatNumber.setTextContent(VAT_NUMBER);
    extensions.getUnknownXMLObjects().add(vatNumber);
    XSAny fiscalCode = new XSAnyBuilder().buildObject(namespaceUri, LOCAL_NAME_FISCAL_CODE,
        namespacePrefix);
    fiscalCode.setTextContent(FISCAL_CODE);
    extensions.getUnknownXMLObjects().add(fiscalCode);

    XSAny ipaCode = new XSAnyBuilder().buildObject(namespaceUri, LOCAL_NAME_IPA,
        namespacePrefix);
    ipaCode.setTextContent(IPA_CODE);
    extensions.getUnknownXMLObjects().add(ipaCode);

    if (namespacePrefix.equals(NAMESPACE_PREFIX_CIE)) {
      XSAny pub = new XSAnyBuilder().buildObject(namespaceUri,
          LOCAL_NAME_PUBLIC,
          namespacePrefix);
      pub.setTextContent("");
      extensions.getUnknownXMLObjects().add(pub);
      XSAny municipality = new XSAnyBuilder().buildObject(namespaceUri, LOCAL_NAME_MUNICIPALITY,
          namespacePrefix);
      municipality.setTextContent(MUNICIPALITY);
      extensions.getUnknownXMLObjects().add(municipality);
    } else {
      XSAny publicServicesFullOperator = new XSAnyBuilder().buildObject(namespaceUri,
          LOCAL_NAME_PUBLIC_SERVICE_FULL_OPERATOR,
          namespacePrefix);
      extensions.getUnknownXMLObjects().add(publicServicesFullOperator);
    }

    return extensions;
  }

  public SPSSODescriptor buildSPSSODescriptor() {
    SPSSODescriptor spssoDescriptor = buildSAMLObject(SPSSODescriptor.class);
    spssoDescriptor.setAuthnRequestsSigned(true);
    spssoDescriptor.setWantAssertionsSigned(true);

    return spssoDescriptor;

  }

  public NameIDFormat buildNameIDFormat() {
    NameIDFormat nameIDFormat = buildSAMLObject(NameIDFormat.class);
    nameIDFormat.setURI(NameIDType.TRANSIENT);

    return nameIDFormat;
  }

  public AssertionConsumerService buildAssertionConsumerService() {
    AssertionConsumerService assertionConsumerService = buildSAMLObject(
        AssertionConsumerService.class);
    assertionConsumerService.setIndex(0);
    assertionConsumerService.setIsDefault(true);
    assertionConsumerService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
    assertionConsumerService.setLocation(BASE_PATH + ACS_URL);

    return assertionConsumerService;
  }

  public SingleLogoutService buildSingleLogoutService() {
    SingleLogoutService singleLogoutService = buildSAMLObject(SingleLogoutService.class);
    singleLogoutService.setLocation(BASE_PATH + SLO_URL);
    singleLogoutService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);

    return singleLogoutService;
  }

  public Organization buildOrganization() {
    Organization organization = buildSAMLObject(Organization.class);
    organization.getOrganizationNames().add(buildOrganizationName());
    organization.getDisplayNames().add(buildDisplayName());
    organization.getURLs().add(buildOrganizationURL());

    return organization;
  }

  public OrganizationName buildOrganizationName() {
    OrganizationName orgName = buildSAMLObject(OrganizationName.class);
    orgName.setValue(ORGANIZATION_NAME);
    orgName.setXMLLang(ORGANIZATION_NAME_XML_LANG);

    return orgName;
  }

  public OrganizationDisplayName buildDisplayName() {
    OrganizationDisplayName orgDisplayName = buildSAMLObject(OrganizationDisplayName.class);
    orgDisplayName.setValue(ORGANIZATION_DISPLAY_NAME);
    orgDisplayName.setXMLLang(ORGANIZATION_DISPLAY_NAME_XML_LANG);

    return orgDisplayName;
  }

  public OrganizationURL buildOrganizationURL() {
    OrganizationURL organizationURL = buildSAMLObject(OrganizationURL.class);
    organizationURL.setURI(ORGANIZATION_URL);
    organizationURL.setXMLLang(ORGANIZATION_URL_XML_LANG);

    return organizationURL;
  }

  public ContactPerson buildContactPerson(String namespacePrefix, String namespaceUri) {
    ContactPerson contactPerson = buildSAMLObject(ContactPerson.class);
    contactPerson.setCompany(buildCompany());
    contactPerson.setType(
        (namespacePrefix.equals(NAMESPACE_PREFIX_CIE)) ? ContactPersonTypeEnumeration.ADMINISTRATIVE
            : ContactPersonTypeEnumeration.OTHER);
    contactPerson.getEmailAddresses().add(buildEmailAddress());
    contactPerson.setExtensions(buildExtensions(namespacePrefix, namespaceUri));
    if (namespacePrefix.equals(NAMESPACE_PREFIX_SPID)) {
      contactPerson.getUnknownAttributes().put(
          new QName(IdType.spid.getNamespaceUri(), "entityType", IdType.spid.getNamespacePrefix()),
          IdType.spid.getNamespacePrefix() + ":" + SPID_AGGREGATOR);
    }
    return contactPerson;
  }

  public Company buildCompany() {
    Company company = buildSAMLObject(Company.class);
    company.setValue(CONTACT_PERSON_COMPANY);

    return company;
  }

  public EmailAddress buildEmailAddress() {
    EmailAddress emailAddress = buildSAMLObject(EmailAddress.class);
    emailAddress.setURI(CONTACT_PERSON_EMAIL_ADDRESS);

    return emailAddress;
  }

}
