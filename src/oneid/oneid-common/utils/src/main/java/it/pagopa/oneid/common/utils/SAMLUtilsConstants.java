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
      @ConfigProperty(name = "metadata_url") String metadata_url,
      @ConfigProperty(name = "acs_url") String acs_url,
      @ConfigProperty(name = "slo_url") String slo_url,
      @ConfigProperty(name = "service_provider_uri") String service_provider_uri,
      @ConfigProperty(name = "organization_name") String organization_name,
      @ConfigProperty(name = "organization_display_name") String organization_display_name,
      @ConfigProperty(name = "organization_url") String organization_url,
      @ConfigProperty(name = "contact_person_company") String contact_person_company,
      @ConfigProperty(name = "contact_person_given_name") String contact_person_given_name,
      @ConfigProperty(name = "contact_person_email_address") String contact_person_email_address,
      @ConfigProperty(name = "contact_person_telephone_number") String contact_person_telephone_number
  ) {
    METADATA_URL = metadata_url;
    ACS_URL = acs_url;
    SLO_URL = slo_url;
    SERVICE_PROVIDER_URI = service_provider_uri;
    ORGANIZATION_NAME = organization_name;
    ORGANIZATION_DISPLAY_NAME = organization_display_name;
    ORGANIZATION_URL = organization_url;
    CONTACT_PERSON_COMPANY = contact_person_company;
    CONTACT_PERSON_GIVEN_NAME = contact_person_given_name;
    CONTACT_PERSON_EMAIL_ADDRESS = contact_person_email_address;
    CONTACT_PERSON_TELEPHONE_NUMBER = contact_person_telephone_number;
  }

}
