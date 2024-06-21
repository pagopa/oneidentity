package it.pagopa.oneid.common.utils;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public abstract class SAMLUtilsConstants {


  @ConfigProperty(name = "metadata_url")
  public static String METADATA_URL;
  @ConfigProperty(name = "service_provider_uri")
  public static String SERVICE_PROVIDER_URI;
  @ConfigProperty(name = "acs_url")
  public static String ACS_URL;
  @ConfigProperty(name = "organization_name")
  public static String ORGANIZATION_NAME;
  @ConfigProperty(name = "organization_name_xml_lang")
  public static String ORGANIZATION_NAME_XML_LANG;
  @ConfigProperty(name = "organization_display_name")
  public static String ORGANIZATION_DISPLAY_NAME;
  @ConfigProperty(name = "organization_display_name_xml_lang")
  public static String ORGANIZATION_DISPLAY_NAME_XML_LANG;
  @ConfigProperty(name = "organization_url")
  public static String ORGANIZATION_URL;
  @ConfigProperty(name = "organization_url_xml_lang")
  public static String ORGANIZATION_URL_XML_LANG;
  @ConfigProperty(name = "contact_person_company")
  public static String CONTACT_PERSON_COMPANY;
  @ConfigProperty(name = "contact_person_given_name")
  public static String CONTACT_PERSON_GIVEN_NAME;
  @ConfigProperty(name = "contact_person_email_address")
  public static String CONTACT_PERSON_EMAIL_ADDRESS;
  @ConfigProperty(name = "contact_person_telephone_number")
  public static String CONTACT_PERSON_TELEPHONE_NUMBER;
  @ConfigProperty(name = "namespace_uri")
  public static String NAMESPACE_URI;
  @ConfigProperty(name = "namespace_prefix")
  public static String NAMESPACE_PREFIX;
  @ConfigProperty(name = "local_name_ipa")
  public static String LOCAL_NAME_IPA;
  @ConfigProperty(name = "ipa_code")
  public static String IPA_CODE;
  @ConfigProperty(name = "local_name_public")
  public static String LOCAL_NAME_PUBLIC;
}
