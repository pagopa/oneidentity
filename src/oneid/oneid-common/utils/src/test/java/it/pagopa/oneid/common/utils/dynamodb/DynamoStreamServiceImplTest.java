package it.pagopa.oneid.common.utils.dynamodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DynamoStreamServiceImplTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final DynamoStreamService dynamoStreamService = new DynamoStreamServiceImpl(objectMapper);

  @Test
  @DisplayName("given new image when extracting client then build client from stream payload")
  void given_new_image_when_extracting_client_then_build_client_from_stream_payload() {
    Optional<Client> result = dynamoStreamService.extractClient(buildRecord("INSERT", null, baseImage()), false);

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
    assertEquals(Set.of("https://callback.example"), result.get().getCallbackURI());
    assertEquals(AuthLevel.L2, result.get().getAuthLevel());
    assertEquals(SamlBinding.HTTP_POST, result.get().getSamlBinding());
    assertTrue(result.get().isActive());
    assertTrue(result.get().isPairwise());
  }

  @Test
  @DisplayName("given old image when extracting client id then return id from old image")
  void given_old_image_when_extracting_client_id_then_return_id_from_old_image() {
    assertEquals("client-old", dynamoStreamService.extractClientId(
        buildRecord("REMOVE", imageWithClientId("client-old"), null), true).orElseThrow());
  }

  @Test
  @DisplayName("given null or blank client id when extracting id then return empty")
  void given_null_or_blank_client_id_when_extracting_id_then_return_empty() {
    assertTrue(dynamoStreamService.extractClientId(null, false).isEmpty());
    assertTrue(dynamoStreamService.extractClientId(
        buildRecord("REMOVE", imageWithClientId(" "), null), true).isEmpty());
  }

  @Test
  @DisplayName("given relevant modify change when checking diff then return true")
  void given_relevant_modify_change_when_checking_diff_then_return_true() {
    ObjectNode newImage = baseImage();
    newImage.set("pairwise", boolValue(false));

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", baseImage(), newImage)));
  }

  @Test
  @DisplayName("given frontend-only modify change when checking diff then return false")
  void given_frontend_only_modify_change_when_checking_diff_then_return_false() {
    ObjectNode oldImage = baseImage();
    ObjectNode newImage = baseImage();
    oldImage.set("logoUri", stringValue("https://logo.old"));
    newImage.set("logoUri", stringValue("https://logo.new"));

    assertFalse(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", oldImage, newImage)));
  }

  @Test
  @DisplayName("given invalid auth level when extracting client then throw")
  void given_invalid_auth_level_when_extracting_client_then_throw() {
    ObjectNode image = baseImage();
    image.set("authLevel", stringValue("invalid-auth-level"));

    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(buildRecord("INSERT", null, image), false));
  }

  @Test
  @DisplayName("given invalid saml binding when extracting client then use HTTP_POST")
  void given_invalid_saml_binding_when_extracting_client_then_use_http_post() {
    ObjectNode image = baseImage();
    image.set("samlBinding", stringValue("invalid-saml-binding"));

    assertEquals(SamlBinding.HTTP_POST, dynamoStreamService.extractClient(
        buildRecord("INSERT", null, image), false).orElseThrow().getSamlBinding());
  }

  @Test
  @DisplayName("given empty image when extracting client then return empty")
  void given_empty_image_when_extracting_client_then_return_empty() {
    assertTrue(dynamoStreamService.extractClient(
        buildRecord("INSERT", null, objectMapper.createObjectNode()), false).isEmpty());
  }

  @Test
  @DisplayName("given rich dynamo payload when extracting client then ignore extra shapes")
  void given_rich_dynamo_payload_when_extracting_client_then_ignore_extra_shapes() {
    ObjectNode image = baseImage();
    image.put("rawText", "text");
    image.put("rawNumber", 7);
    image.put("rawBoolean", false);
    ArrayNode values = objectMapper.createObjectNode().putArray("L");
    values.addObject().put("S", "nested");
    image.set("rawList", objectMapper.createObjectNode().set("L", values));

    assertTrue(dynamoStreamService.extractClient(
        buildRecord("INSERT", null, image), false).isPresent());
  }

  @Test
  @DisplayName("given nested map and explicit null attributes when extracting client then preserve payload")
  void given_nested_map_and_explicit_null_attributes_when_extracting_client_then_preserve_payload()
      throws Exception {
    ObjectNode image = baseImage();
    ObjectNode localizedContent = objectMapper.createObjectNode();
    localizedContent.put("title", "Welcome");
    localizedContent.put("description", "Description");
    ObjectNode languageContent = objectMapper.createObjectNode();
    languageContent.set("home", objectMapper.createObjectNode()
        .set("M", localizedContent));
    image.set("localizedContentMap", objectMapper.createObjectNode()
        .set("M", objectMapper.createObjectNode().set("en", objectMapper.createObjectNode()
            .set("M", languageContent))));
    image.set("logoUri", objectMapper.createObjectNode().put("NULL", true));
    image.set("clientIdIssuedAt", objectMapper.createObjectNode().put("N", "2147483648"));

    Client client = dynamoStreamService.extractClient(
        buildRecord("INSERT", null, image), false).orElseThrow();

    assertEquals("Welcome", client.getLocalizedContentMap().get("en").get("home").title());
    assertEquals(2147483648L, client.getClientIdIssuedAt());
    assertNull(client.getLogoUri());
  }

  @Test
  @DisplayName("given empty localized content map when extracting frontend client then use empty map")
  void given_empty_localized_content_map_when_extracting_frontend_client_then_use_empty_map() {
    ObjectNode image = baseImage();
    image.set("localizedContentMap", stringValue(""));

    assertTrue(dynamoStreamService.extractClient(
        buildRecord("INSERT", null, image), false).isPresent());

    var clientFE = dynamoStreamService.extractClientFE(
        buildRecord("INSERT", null, image), false).orElseThrow();
    assertTrue(clientFE.getLocalizedContentMap().isEmpty());
  }

  private JsonNode buildRecord(String eventName, ObjectNode oldImage, ObjectNode newImage) {
    ObjectNode record = objectMapper.createObjectNode().put("eventName", eventName);
    ObjectNode dynamodb = record.putObject("dynamodb");
    if (oldImage != null) dynamodb.set("OldImage", oldImage);
    if (newImage != null) dynamodb.set("NewImage", newImage);
    return record;
  }

  private ObjectNode baseImage() {
    ObjectNode image = objectMapper.createObjectNode();
    image.set("clientId", stringValue("client-test"));
    image.set("callbackURI", stringSetValue("https://callback.example"));
    image.set("requestedParameters", stringSetValue("fiscalNumber"));
    image.set("authLevel", stringValue("https://www.spid.gov.it/SpidL2"));
    image.set("samlBinding", stringValue("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"));
    image.set("acsIndex", numberValue(1));
    image.set("attributeIndex", numberValue(2));
    image.set("active", boolValue(true));
    image.set("requiredSameIdp", boolValue(true));
    image.set("pairwise", boolValue(true));
    image.set("spidMinors", boolValue(false));
    image.set("spidProfessionals", boolValue(false));
    image.set("minAge", numberValue(0));
    image.set("maxAge", numberValue(0));
    image.set("ageParentAuth", numberValue(0));
    return image;
  }

  private ObjectNode imageWithClientId(String value) {
    return objectMapper.createObjectNode().set("clientId", stringValue(value));
  }

  private ObjectNode stringValue(String value) {
    return objectMapper.createObjectNode().put("S", value);
  }

  private ObjectNode numberValue(long value) {
    return objectMapper.createObjectNode().put("N", String.valueOf(value));
  }

  private ObjectNode boolValue(boolean value) {
    return objectMapper.createObjectNode().put("BOOL", value);
  }

  private ObjectNode stringSetValue(String value) {
    ObjectNode attributeValue = objectMapper.createObjectNode();
    attributeValue.putArray("SS").add(value);
    return attributeValue;
  }
}
