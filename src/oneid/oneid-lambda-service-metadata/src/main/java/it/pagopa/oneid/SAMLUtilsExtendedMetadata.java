package it.pagopa.oneid;

import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ACS_URL;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.CONTACT_PERSON_COMPANY;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.CONTACT_PERSON_EMAIL_ADDRESS;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.CONTACT_PERSON_GIVEN_NAME;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.CONTACT_PERSON_TELEPHONE_NUMBER;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.IPA_CODE;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.LOCAL_NAME_IPA;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.LOCAL_NAME_PUBLIC;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.NAMESPACE_PREFIX;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.NAMESPACE_URI;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.NAME_FORMAT;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_DISPLAY_NAME_XML_LANG;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_NAME;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_NAME_XML_LANG;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_URL;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_URL_XML_LANG;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.SLO_URL;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.enums.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
import org.opensaml.saml.saml2.metadata.GivenName;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
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
import org.opensaml.saml.saml2.metadata.TelephoneNumber;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.UsageType;

@ApplicationScoped
public class SAMLUtilsExtendedMetadata extends SAMLUtils {

  @Inject
  SAMLUtilsConstants samlUtilsConstants;

  public SAMLUtilsExtendedMetadata() throws SAMLUtilsException {
    super();
  }

  public static SPSSODescriptor buildSPSSODescriptor() {
    SPSSODescriptor spssoDescriptor = buildSAMLObject(SPSSODescriptor.class);
    spssoDescriptor.setAuthnRequestsSigned(true);
    spssoDescriptor.setWantAssertionsSigned(true);

    return spssoDescriptor;

  }

  public static KeyDescriptor buildKeyDescriptor() throws SAMLUtilsException {
    KeyDescriptor signKeyDescriptor = buildSAMLObject(KeyDescriptor.class);

    signKeyDescriptor.setUse(UsageType.SIGNING);  //Set usage
    try {
      signKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(X509Credential));
    } catch (SecurityException e) {
      throw new SAMLUtilsException(e);
    }
    return signKeyDescriptor;
  }

  public static NameIDFormat buildNameIDFormat() {
    NameIDFormat nameIDFormat = buildSAMLObject(NameIDFormat.class);
    nameIDFormat.setURI(NameIDType.TRANSIENT);

    return nameIDFormat;
  }

  public static AssertionConsumerService buildAssertionConsumerService() {
    AssertionConsumerService assertionConsumerService = buildSAMLObject(
        AssertionConsumerService.class);
    assertionConsumerService.setIndex(0);
    assertionConsumerService.setIsDefault(true);
    assertionConsumerService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
    assertionConsumerService.setLocation(ACS_URL);

    return assertionConsumerService;
  }

  public static SingleLogoutService buildSingleLogoutService() {
    SingleLogoutService singleLogoutService = buildSAMLObject(SingleLogoutService.class);
    singleLogoutService.setLocation(SLO_URL);
    singleLogoutService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);

    return singleLogoutService;
  }

  public static Organization buildOrganization() {
    Organization organization = buildSAMLObject(Organization.class);
    organization.getOrganizationNames().add(buildOrganizationName());
    organization.getDisplayNames().add(buildDisplayName());
    organization.getURLs().add(buildOrganizationURL());

    return organization;
  }

  public static OrganizationName buildOrganizationName() {
    OrganizationName orgName = buildSAMLObject(OrganizationName.class);
    orgName.setValue(ORGANIZATION_NAME);
    orgName.setXMLLang(ORGANIZATION_NAME_XML_LANG);

    return orgName;
  }

  public static OrganizationDisplayName buildDisplayName() {
    OrganizationDisplayName orgDisplayName = buildSAMLObject(OrganizationDisplayName.class);
    orgDisplayName.setValue(SAMLUtilsConstants.ORGANIZATION_DISPLAY_NAME);
    orgDisplayName.setXMLLang(ORGANIZATION_DISPLAY_NAME_XML_LANG);

    return orgDisplayName;
  }

  public static OrganizationURL buildOrganizationURL() {
    OrganizationURL organizationURL = buildSAMLObject(OrganizationURL.class);
    organizationURL.setURI(ORGANIZATION_URL);
    organizationURL.setXMLLang(ORGANIZATION_URL_XML_LANG);

    return organizationURL;
  }

  public static ContactPerson buildContactPerson() {
    ContactPerson contactPerson = buildSAMLObject(ContactPerson.class);
    contactPerson.setCompany(buildCompany());
    contactPerson.setType(ContactPersonTypeEnumeration.OTHER);
    contactPerson.setGivenName(buildGivenName());
    contactPerson.getEmailAddresses().add(buildEmailAddress());
    contactPerson.getTelephoneNumbers().add(buildTelephoneNumber());
    contactPerson.setExtensions(buildExtensions());

    return contactPerson;
  }

  public static Company buildCompany() {
    Company company = buildSAMLObject(Company.class);
    company.setValue(CONTACT_PERSON_COMPANY);

    return company;
  }


  public static GivenName buildGivenName() {
    GivenName givenName = buildSAMLObject(GivenName.class);
    givenName.setValue(CONTACT_PERSON_GIVEN_NAME);

    return givenName;
  }

  public static EmailAddress buildEmailAddress() {
    EmailAddress emailAddress = buildSAMLObject(EmailAddress.class);
    emailAddress.setURI(CONTACT_PERSON_EMAIL_ADDRESS);

    return emailAddress;
  }

  public static TelephoneNumber buildTelephoneNumber() {
    TelephoneNumber telephoneNumber = buildSAMLObject(TelephoneNumber.class);
    telephoneNumber.setValue(CONTACT_PERSON_TELEPHONE_NUMBER);

    return telephoneNumber;
  }

  public static Extensions buildExtensions() {
    Extensions extensions = buildSAMLObject(Extensions.class);
    XSAny ipaCode = new XSAnyBuilder().buildObject(NAMESPACE_URI, LOCAL_NAME_IPA, NAMESPACE_PREFIX);
    ipaCode.setTextContent(IPA_CODE);
    extensions.getUnknownXMLObjects().add(ipaCode);
    XSAny pub = new XSAnyBuilder().buildObject(NAMESPACE_URI, LOCAL_NAME_PUBLIC, NAMESPACE_PREFIX);
    pub.setTextContent("");
    extensions.getUnknownXMLObjects().add(pub);

    return extensions;
  }

  public static AttributeConsumingService buildAttributeConsumingService(Client client) {
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

  public static ServiceName buildServiceName(String name) {
    ServiceName serviceName = buildSAMLObject(ServiceName.class);
    serviceName.setValue(name);
    // TODO consider adding a field for xml lang on service name
    serviceName.setXMLLang("it");

    return serviceName;
  }

  public static ServiceDescription buildServiceDescription(String description) {
    ServiceDescription serviceDescription = buildSAMLObject(ServiceDescription.class);
    serviceDescription.setValue(description);
    // TODO consider adding a field for xml lang on service description
    serviceDescription.setXMLLang("it");

    return serviceDescription;
  }

  public static RequestedAttribute buildRequestedAttribute(String requestedParameter) {
    RequestedAttribute attribute = buildSAMLObject(RequestedAttribute.class);
    attribute.setNameFormat(NAME_FORMAT);
    attribute.setName(Identifier.valueOf(requestedParameter).name());
    attribute.setFriendlyName(Identifier.valueOf(requestedParameter).getFriendlyName());

    return attribute;
  }


}
