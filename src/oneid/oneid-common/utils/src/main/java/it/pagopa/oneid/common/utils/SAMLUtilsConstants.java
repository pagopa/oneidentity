package it.pagopa.oneid.common.utils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class SAMLUtilsConstants {

  public static String ENTITY_ID;
  public static String BASE_PATH;
  public static String ACS_URL;
  public static String SLO_URL;
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
  public static String NAMESPACE_URI_SPID = "https://spid.gov.it/saml-extensions";
  public static String NAMESPACE_URI_CIE = "https://www.cartaidentita.interno.gov.it/saml-extensions";
  public static String NAMESPACE_PREFIX_SPID = "spid";
  public static String NAMESPACE_PREFIX_CIE = "cie";
  public static String LOCAL_NAME_IPA = "IPACode";
  public static String LOCAL_NAME_PUBLIC_SERVICE_AGGREGATOR = "PublicServicesFullAggregator";
  public static String LOCAL_NAME_VAT_NUMBER = "VATNumber";
  public static String LOCAL_NAME_FISCAL_CODE = "FiscalCode";
  public static String LOCAL_NAME_MUNICIPALITY = "Municipality";
  public static String IPA_CODE = "5N2TR557";
  public static String VAT_NUMBER = "IT15376371009";
  public static String FISCAL_CODE = "15376371009";
  public static String LOCAL_NAME_PUBLIC = "Public";
  public static String NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";
  public static String MUNICIPALITY = "H501";
  public static String SPID_AGGREGATED = "aggregated";
  public static String SPID_AGGREGATOR = "aggregator";

  @Inject
  SAMLUtilsConstants(
      @ConfigProperty(name = "entity_id") String entityId,
      @ConfigProperty(name = "base_path") String basePath,
      @ConfigProperty(name = "acs_url") String acsUrl,
      @ConfigProperty(name = "slo_url") String sloUrl,
      @ConfigProperty(name = "organization_name") String organizationName,
      @ConfigProperty(name = "organization_display_name") String organizationDisplayName,
      @ConfigProperty(name = "organization_url") String organizationUrl,
      @ConfigProperty(name = "contact_person_company") String contactPersonCompany,
      @ConfigProperty(name = "contact_person_given_name") String contactPersonGivenName,
      @ConfigProperty(name = "contact_person_email_address") String contactPersonEmailAddress,
      @ConfigProperty(name = "contact_person_telephone_number") String contactPersonTelephoneNumber
  ) {
    ENTITY_ID = entityId;
    BASE_PATH = basePath;
    ACS_URL = acsUrl;
    SLO_URL = sloUrl;
    ORGANIZATION_NAME = organizationName;
    ORGANIZATION_DISPLAY_NAME = organizationDisplayName;
    ORGANIZATION_URL = organizationUrl;
    CONTACT_PERSON_COMPANY = contactPersonCompany;
    CONTACT_PERSON_GIVEN_NAME = contactPersonGivenName;
    CONTACT_PERSON_EMAIL_ADDRESS = contactPersonEmailAddress;
    CONTACT_PERSON_TELEPHONE_NUMBER = contactPersonTelephoneNumber;
  }

}
