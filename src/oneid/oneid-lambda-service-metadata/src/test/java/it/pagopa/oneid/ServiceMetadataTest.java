package it.pagopa.oneid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import it.pagopa.oneid.common.model.Client;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

class ServiceMetadataTest {

  @BeforeAll
  static void initializeOpenSaml() throws Exception {
    InitializationService.initialize();
  }

  @Test
  void buildAssertionConsumerService_alwaysUsesPostBinding() {
    SAMLUtilsExtendedMetadata samlUtilsExtendedMetadata = new SAMLUtilsExtendedMetadata();
    samlUtilsExtendedMetadata.BASE_PATH = "https://example.com";
    samlUtilsExtendedMetadata.ACS_URL = "/acs";

    AssertionConsumerService assertionConsumerService = samlUtilsExtendedMetadata
        .buildAssertionConsumerService(7, true);

    assertEquals(7, assertionConsumerService.getIndex());
    assertTrue(assertionConsumerService.isDefault());
    assertEquals(SAMLConstants.SAML2_POST_BINDING_URI, assertionConsumerService.getBinding());
    assertEquals("https://example.com/acs", assertionConsumerService.getLocation());
  }

  @Test
  void buildAssertionConsumerServices_marksOnlyIndexZeroAsDefault_andDeduplicatesIndices() {
    SAMLUtilsExtendedMetadata samlUtilsExtendedMetadata = new SAMLUtilsExtendedMetadata();
    samlUtilsExtendedMetadata.BASE_PATH = "https://example.com";
    samlUtilsExtendedMetadata.ACS_URL = "/acs";
    ServiceMetadata serviceMetadata = new ServiceMetadata();
    serviceMetadata.samlUtils = samlUtilsExtendedMetadata;
    Client minorsClient = Client.builder()
        .clientId("minors-client")
        .acsIndex(7)
      .attributeIndex(1)
      .friendlyName("Minors client")
      .requestedParameters(Set.of())
        .spidMinors(true)
        .build();
    Client clientAtDefaultIndex = Client.builder()
        .clientId("default-client")
        .acsIndex(0)
      .attributeIndex(2)
      .friendlyName("Default client")
      .requestedParameters(Set.of())
        .build();
    Client duplicateIndexClient = Client.builder()
        .clientId("duplicate-index-client")
        .acsIndex(7)
      .attributeIndex(3)
      .friendlyName("Duplicate index client")
      .requestedParameters(Set.of())
        .build();

    SPSSODescriptor descriptor = samlUtilsExtendedMetadata.buildSPSSODescriptor();
    serviceMetadata.addClientMetadata(descriptor,
      List.of(minorsClient, clientAtDefaultIndex, duplicateIndexClient));
    var assertionConsumerServices = descriptor.getAssertionConsumerServices();

    assertEquals(2, assertionConsumerServices.size());
    assertEquals(3, descriptor.getAttributeConsumingServices().size());
    assertEquals(1L, assertionConsumerServices.stream().filter(acs -> acs.isDefault()).count());
    assertTrue(assertionConsumerServices.stream()
        .anyMatch(acs -> acs.getIndex() == 0 && acs.isDefault()));
    assertTrue(assertionConsumerServices.stream()
        .filter(acs -> acs.getIndex() != 0)
        .noneMatch(acs -> acs.isDefault()));
    assertTrue(assertionConsumerServices.stream()
        .allMatch(acs -> SAMLConstants.SAML2_POST_BINDING_URI.equals(acs.getBinding())));
    assertTrue(assertionConsumerServices.stream()
        .allMatch(acs -> "https://example.com/acs".equals(acs.getLocation())));

    SPSSODescriptor reverseOrderDescriptor = samlUtilsExtendedMetadata.buildSPSSODescriptor();
    serviceMetadata.addClientMetadata(reverseOrderDescriptor,
      List.of(clientAtDefaultIndex, minorsClient));
    var reverseOrderAssertionConsumerServices = reverseOrderDescriptor
      .getAssertionConsumerServices();

    assertTrue(reverseOrderAssertionConsumerServices.stream()
        .anyMatch(acs -> acs.getIndex() == 0 && acs.isDefault()));
    assertTrue(reverseOrderAssertionConsumerServices.stream()
        .filter(acs -> acs.getIndex() != 0)
        .noneMatch(acs -> acs.isDefault()));
  }

  @Test
  void buildEntityExtensions_preservesUpdatedMinorsAcsIndexAndAgeConstraints() {
    SAMLUtilsExtendedMetadata samlUtilsExtendedMetadata = new SAMLUtilsExtendedMetadata();
    Client minorsClient = Client.builder()
        .clientId("minors-client")
        .acsIndex(7)
        .spidMinors(true)
        .minAge(6)
        .maxAge(17)
        .ageParentAuth(14)
        .build();

    Extensions extensions = samlUtilsExtendedMetadata.buildEntityExtensions(
        Map.of(minorsClient.getClientId(), minorsClient));

    assertNotNull(extensions);
    assertEquals(1, extensions.getUnknownXMLObjects().size());
    XSAny ageLimit = (XSAny) extensions.getUnknownXMLObjects().get(0);
    assertEquals("AgeLimit", ageLimit.getElementQName().getLocalPart());
    assertEquals(4, ageLimit.getUnknownXMLObjects().size());
    assertAgeLimitValue(ageLimit, 0, "AssertionConsumerServiceIndex", "7");
    assertAgeLimitValue(ageLimit, 1, "MinAge", "6");
    assertAgeLimitValue(ageLimit, 2, "MaxAge", "17");
    assertAgeLimitValue(ageLimit, 3, "AgeParentAuth", "14");

    Client regularClient = Client.builder()
        .clientId("regular-client")
        .acsIndex(0)
        .spidMinors(false)
        .build();
    assertNull(samlUtilsExtendedMetadata.buildEntityExtensions(
        Map.of(regularClient.getClientId(), regularClient)));
  }

  private void assertAgeLimitValue(XSAny ageLimit, int index, String elementName,
      String expectedValue) {
    XSAny ageLimitField = (XSAny) ageLimit.getUnknownXMLObjects().get(index);
    assertEquals(elementName, ageLimitField.getElementQName().getLocalPart());
    assertEquals(expectedValue, ageLimitField.getTextContent());
  }

  @Test
  void hasMetadataChanged_ignoresSamlBindingChanges() throws Exception {
    ServiceMetadata serviceMetadata = new ServiceMetadata();
    JsonNode node = JsonMapper.builder().build().readTree("""
        {
          "dynamodb": {
            "OldImage": {
              "samlBinding": {"S": "HTTP-POST"}
            },
            "NewImage": {
              "samlBinding": {"S": "HTTP-Redirect"}
            }
          }
        }
        """);

    Method hasMetadataChanged = ServiceMetadata.class.getDeclaredMethod("hasMetadataChanged",
        JsonNode.class);
    hasMetadataChanged.setAccessible(true);

    boolean changed = (boolean) hasMetadataChanged.invoke(serviceMetadata, node);

    assertFalse(changed);
  }

  @Test
  void hasMetadataChanged_detectsOtherRelevantFields() throws Exception {
    ServiceMetadata serviceMetadata = new ServiceMetadata();
    JsonNode node = JsonMapper.builder().build().readTree("""
        {
          "dynamodb": {
            "OldImage": {
              "acsIndex": {"N": "1"}
            },
            "NewImage": {
              "acsIndex": {"N": "2"}
            }
          }
        }
        """);

    Method hasMetadataChanged = ServiceMetadata.class.getDeclaredMethod("hasMetadataChanged",
        JsonNode.class);
    hasMetadataChanged.setAccessible(true);

    boolean changed = (boolean) hasMetadataChanged.invoke(serviceMetadata, node);

    assertTrue(changed);
  }

  @Test
  void hasMetadataChanged_detectsEachSpidMinorsMetadataChange() throws Exception {
    ServiceMetadata serviceMetadata = new ServiceMetadata();
    JsonMapper mapper = JsonMapper.builder().build();
    Method hasMetadataChanged = ServiceMetadata.class.getDeclaredMethod("hasMetadataChanged",
        JsonNode.class);
    hasMetadataChanged.setAccessible(true);
    String[][] changedFields = {
        {"acsIndex", "N", "1", "7"},
        {"spidMinors", "BOOL", "false", "true"},
        {"minAge", "N", "6", "8"},
        {"maxAge", "N", "17", "16"},
        {"ageParentAuth", "N", "14", "16"}
    };

    for (String[] changedField : changedFields) {
      JsonNode node = mapper.readTree("""
          {
            "dynamodb": {
              "OldImage": {"%s": {"%s": "%s"}},
              "NewImage": {"%s": {"%s": "%s"}}
            }
          }
          """.formatted(changedField[0], changedField[1], changedField[2],
          changedField[0], changedField[1], changedField[3]));

      boolean changed = (boolean) hasMetadataChanged.invoke(serviceMetadata, node);

      assertTrue(changed, "Expected metadata change detection for " + changedField[0]);
    }
  }
}
