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
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_DISPLAY_NAME;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_DISPLAY_NAME_XML_LANG;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_NAME;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_NAME_XML_LANG;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_URL;
import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.ORGANIZATION_URL_XML_LANG;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.SAMLUtils;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Company;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.EmailAddress;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.GivenName;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.OrganizationName;
import org.opensaml.saml.saml2.metadata.OrganizationURL;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.TelephoneNumber;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.UsageType;

public class SAMLUtilsExtended extends SAMLUtils {


  public SAMLUtilsExtended() throws SAMLUtilsException {
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
    orgDisplayName.setValue(ORGANIZATION_DISPLAY_NAME);
    orgDisplayName.setValue(ORGANIZATION_DISPLAY_NAME_XML_LANG);

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


}
