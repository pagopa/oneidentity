package it.pagopa.oneid.common.utils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class SAMLUtilsConstants {

  public static String METADATA_URL;
  public static String ACS_URL;
  public static String SLO_URL;
  public static String SERVICE_PROVIDER_URI;
  public static String ORGANIZATION_NAME;
  public static String ORGANIZATION_NAME_XML_LANG = "it";
  public static String ORGANIZATION_DISPLAY_NAME;
  public static String ORGANIZATION_DISPLAY_NAME_XML_LANG = "it";
  public static String ORGANIZATION_URL;
  public static String ORGANIZATION_URL_XML_LANG = "it";
  public static String CONTACT_PERSON_COMPANY;
  public static String CONTACT_PERSON_GIVEN_NAME;
  public static String CONTACT_PERSON_EMAIL_ADDRESS;
  public static String CONTACT_PERSON_TELEPHONE_NUMBER;
  public static String NAMESPACE_URI = "https://spid.gov.it/saml-extensions";
  public static String NAMESPACE_PREFIX = "spid";
  public static String LOCAL_NAME_IPA = "IPACode";
  public static String IPA_CODE = "h_c501";
  public static String LOCAL_NAME_PUBLIC = "Public";
  public static String NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";

  @Inject
  SAMLUtilsConstants(
      @ConfigProperty(name = "metadata_url") String metadataUrl,
      @ConfigProperty(name = "acs_url") String acsUrl,
      @ConfigProperty(name = "slo_url") String sloUrl,
      @ConfigProperty(name = "service_provider_uri") String serviceProviderUri,
      @ConfigProperty(name = "organization_name") String organizationName,
      @ConfigProperty(name = "organization_display_name") String organizationDisplayName,
      @ConfigProperty(name = "organization_url") String organizationUrl,
      @ConfigProperty(name = "contact_person_company") String contactPersonCompany,
      @ConfigProperty(name = "contact_person_given_name") String contactPersonGivenName,
      @ConfigProperty(name = "contact_person_email_address") String contactPersonEmailAddress,
      @ConfigProperty(name = "contact_person_telephone_number") String contactPersonTelephoneNumber
  ) {
    METADATA_URL = metadataUrl;
    ACS_URL = acsUrl;
    SLO_URL = sloUrl;
    SERVICE_PROVIDER_URI = serviceProviderUri;
    ORGANIZATION_NAME = organizationName;
    ORGANIZATION_DISPLAY_NAME = organizationDisplayName;
    ORGANIZATION_URL = organizationUrl;
    CONTACT_PERSON_COMPANY = contactPersonCompany;
    CONTACT_PERSON_GIVEN_NAME = contactPersonGivenName;
    CONTACT_PERSON_EMAIL_ADDRESS = contactPersonEmailAddress;
    CONTACT_PERSON_TELEPHONE_NUMBER = contactPersonTelephoneNumber;
  }

}
