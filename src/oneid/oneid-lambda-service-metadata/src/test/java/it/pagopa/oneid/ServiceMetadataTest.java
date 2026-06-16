package it.pagopa.oneid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;

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
}
