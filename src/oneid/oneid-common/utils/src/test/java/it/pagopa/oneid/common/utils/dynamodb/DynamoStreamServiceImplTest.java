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
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.PairwiseMode;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@QuarkusTest
class DynamoStreamServiceImplTest {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  DynamoStreamService dynamoStreamService;

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
    assertEquals(PairwiseMode.TOKEN, result.get().getPairwise());
    assertTrue(result.get().isClientErrorRedirectEnabled());
  }

  @ParameterizedTest
  @ValueSource(strings = {"TOKEN", "PDV"})
  @DisplayName("given pairwise enum string when extracting client then preserve mode")
  void given_pairwise_enum_string_when_extracting_client_then_preserve_mode(String mode) {
    ObjectNode image = baseImage();
    image.set("pairwise", stringValue(mode));

    Client client = dynamoStreamService.extractClient(
        buildRecord("INSERT", null, image), false).orElseThrow();

    assertEquals(PairwiseMode.valueOf(mode), client.getPairwise());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{\"BOOL\":true}", "{\"BOOL\":false}", "{\"N\":\"0\"}", "{\"N\":\"1\"}",
      "{\"SS\":[\"TOKEN\"]}", "{\"L\":[]}", "{\"M\":{}}", "{}",
      "true", "false", "0", "1", "\"TOKEN\"", "[]",
      "{\"S\":true}", "{\"S\":1}", "{\"S\":null}", "{\"NULL\":false}",
      "{\"S\":\"TOKEN\",\"BOOL\":true}", "{\"S\":\"TOKEN\",\"NULL\":true}",
      "{\"S\":\"TOKEN\",\"NULL\":false}", "{\"NULL\":true,\"BOOL\":false}",
      "{\"NULL\":\"true\"}",
      "{\"S\":\"true\"}", "{\"S\":\"false\"}", "{\"S\":\"\"}", "{\"S\":\"  \"}",
      "{\"S\":\"token\"}", "{\"S\":\" TOKEN \"}", "{\"S\":\"UNKNOWN\"}"
  })
  @DisplayName("given unsupported pairwise attribute when extracting client then reject it")
  void given_unsupported_pairwise_attribute_when_extracting_client_then_reject_it(String attribute)
      throws Exception {
    ObjectNode image = baseImage();
    image.set("pairwise", objectMapper.readTree(attribute));

    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(buildRecord("INSERT", null, image), false));
    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(buildRecord("REMOVE", image, null), true));
  }

  @ParameterizedTest
  @ValueSource(strings = {"null", "{\"NULL\":true}"})
  @DisplayName("given null pairwise attribute when extracting client then keep pairwise disabled")
  void given_null_pairwise_attribute_when_extracting_client_then_keep_pairwise_disabled(
      String attribute) throws Exception {
    ObjectNode image = baseImage();
    image.set("pairwise", objectMapper.readTree(attribute));

    assertNull(dynamoStreamService.extractClient(
        buildRecord("INSERT", null, image), false).orElseThrow().getPairwise());
  }

  @Test
  @DisplayName("given absent pairwise attribute when extracting client then keep pairwise disabled")
  void given_absent_pairwise_attribute_when_extracting_client_then_keep_pairwise_disabled() {
    ObjectNode image = baseImage();
    image.remove("pairwise");

    assertNull(dynamoStreamService.extractClient(
        buildRecord("INSERT", null, image), false).orElseThrow().getPairwise());
  }

  @Test
  @DisplayName("given legacy pairwise DynamoDB booleans when converting then reject them")
  void given_legacy_pairwise_dynamodb_booleans_when_converting_then_reject_them() {
    for (boolean value : new boolean[] {true, false}) {
      Map<String, AttributeValue> item = Map.of("pairwise", AttributeValue.builder().bool(value).build());
      assertThrows(IllegalArgumentException.class, () -> TableSchema.fromBean(Client.class).mapToItem(item));
      assertThrows(IllegalArgumentException.class, () -> TableSchema.fromBean(ClientExtended.class).mapToItem(item));
    }
  }

  @Test
  @DisplayName("given pairwise DynamoDB string or null when converting then preserve mode")
  void given_pairwise_dynamodb_string_or_null_when_converting_then_preserve_mode() {
    AttributeValue clientId = AttributeValue.builder().s("client-test").build();
    for (TableSchema<? extends Client> schema : List.of(
        TableSchema.fromBean(Client.class), TableSchema.fromBean(ClientExtended.class))) {
      assertNull(schema.mapToItem(Map.of("clientId", clientId)).getPairwise());
      assertNull(schema.mapToItem(Map.of(
          "clientId", clientId, "pairwise", AttributeValue.builder().nul(true).build())).getPairwise());
      for (PairwiseMode mode : PairwiseMode.values()) {
        assertEquals(mode, schema.mapToItem(
            Map.of("pairwise", AttributeValue.builder().s(mode.name()).build())).getPairwise());
      }
      assertThrows(IllegalArgumentException.class, () -> schema.mapToItem(
          Map.of("pairwise", AttributeValue.builder().s("UNKNOWN").build())));
    }
  }

  @Test
  @DisplayName("given pairwise mode when converting to DynamoDB then write the new representation")
  void given_pairwise_mode_when_converting_to_dynamodb_then_write_the_new_representation() {
    TableSchema<Client> schema = TableSchema.fromBean(Client.class);
    for (PairwiseMode mode : PairwiseMode.values()) {
      Client client = Client.builder().pairwise(mode).build();
      assertEquals(AttributeValue.builder().s(mode.name()).build(),
          schema.itemToMap(client, true).get("pairwise"));
    }
    assertFalse(schema.itemToMap(new Client(), true).containsKey("pairwise"));
    assertEquals(AttributeValue.builder().nul(true).build(),
        schema.itemToMap(new Client(), false).get("pairwise"));
  }

  @Test
  @DisplayName("given legacy image when extracting client then direct redirect defaults to disabled")
  void given_legacy_image_when_extracting_client_then_direct_redirect_defaults_to_disabled() {
    ObjectNode legacyImage = baseImage();
    legacyImage.remove("clientErrorRedirectEnabled");

    Client client = dynamoStreamService.extractClient(
        buildRecord("INSERT", null, legacyImage), false).orElseThrow();

    assertFalse(client.isClientErrorRedirectEnabled());
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
  @DisplayName("given missing or invalid image when extracting client then return empty")
  void given_missing_or_invalid_image_when_extracting_client_then_return_empty() {
    assertTrue(dynamoStreamService.extractClient(buildRecord("INSERT", null, null), false)
        .isEmpty());

    ObjectNode record = objectMapper.createObjectNode();
    record.putObject("dynamodb").put("NewImage", "invalid-image");
    assertTrue(dynamoStreamService.extractClient(record, false).isEmpty());
  }

  @Test
  @DisplayName("given relevant modify change when checking diff then return true")
  void given_relevant_modify_change_when_checking_diff_then_return_true() {
    ObjectNode newImage = baseImage();
    newImage.set("pairwise", stringValue("PDV"));

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", baseImage(), newImage)));
  }

  @Test
  @DisplayName("given pairwise mode removed when checking diff then return true")
  void given_pairwise_mode_removed_when_checking_diff_then_return_true() {
    ObjectNode newImage = baseImage();
    newImage.remove("pairwise");

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", baseImage(), newImage)));
  }

  @Test
  @DisplayName("given absent and null pairwise when checking diff then return false")
  void given_absent_and_null_pairwise_when_checking_diff_then_return_false() {
    ObjectNode oldImage = baseImage();
    oldImage.remove("pairwise");
    ObjectNode newImage = oldImage.deepCopy();
    newImage.set("pairwise", objectMapper.createObjectNode().put("NULL", true));

    assertFalse(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", oldImage, newImage)));
  }

  @Test
  @DisplayName("given direct redirect flag change when checking diff then return true")
  void given_direct_redirect_flag_change_when_checking_diff_then_return_true() {
    ObjectNode newImage = baseImage();
    newImage.set("clientErrorRedirectEnabled", boolValue(false));

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", baseImage(), newImage)));
  }

  @Test
  @DisplayName("given legacy image gains direct redirect flag when checking diff then return true")
  void given_legacy_image_gains_direct_redirect_flag_when_checking_diff_then_return_true() {
    ObjectNode legacyImage = baseImage();
    legacyImage.remove("clientErrorRedirectEnabled");

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", legacyImage, baseImage())));
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
  @DisplayName("given non modify event when checking diff then return true")
  void given_non_modify_event_when_checking_diff_then_return_true() {
    assertTrue(dynamoStreamService.hasCacheRelevantChanges(buildRecord("INSERT", null, null)));
  }

  @Test
  @DisplayName("given incomplete modify image when checking diff then return true")
  void given_incomplete_modify_image_when_checking_diff_then_return_true() {
    assertTrue(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", baseImage(), null)));
    assertTrue(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", null, baseImage())));
  }

  @Test
  @DisplayName("given null dynamodb node when checking diff then return false")
  void given_null_dynamodb_node_when_checking_diff_then_return_false() {
    ObjectNode record = objectMapper.createObjectNode().put("eventName", "MODIFY");
    record.putNull("dynamodb");

    assertFalse(dynamoStreamService.hasCacheRelevantChanges(record));
  }

  @Test
  @DisplayName("given changed eidas index when checking diff then return true")
  void given_changed_eidas_index_when_checking_diff_then_return_true() {
    ObjectNode oldImage = baseImage();
    ObjectNode newImage = baseImage();
    oldImage.set("eidasIndex", numberValue(1));
    newImage.set("eidasIndex", numberValue(2));

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", oldImage, newImage)));
  }

  @Test
  @DisplayName("given changed string set when checking diff then return true")
  void given_changed_string_set_when_checking_diff_then_return_true() {
    ObjectNode oldImage = baseImage();
    ObjectNode newImage = baseImage();
    newImage.set("requestedParameters", stringSetValue("name"));

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(
        buildRecord("MODIFY", oldImage, newImage)));
  }

  @Test
  @DisplayName("given invalid auth level when extracting client then throw")
  void given_invalid_auth_level_when_extracting_client_then_throw() {
    ObjectNode image = baseImage();
    image.set("authLevel", stringValue("invalid-auth-level"));
    JsonNode streamRecord = buildRecord("INSERT", null, image);

    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(streamRecord, false));
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

  @Test
  @DisplayName("given missing required client fields when extracting client then throw")
  void given_missing_required_client_fields_when_extracting_client_then_throw() {
    ObjectNode withoutCallback = baseImage();
    withoutCallback.remove("callbackURI");
    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(
            buildRecord("INSERT", null, withoutCallback), false));

    ObjectNode withoutRequestedParameters = baseImage();
    withoutRequestedParameters.remove("requestedParameters");
    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(
            buildRecord("INSERT", null, withoutRequestedParameters), false));

    ObjectNode withoutAuthLevel = baseImage();
    withoutAuthLevel.remove("authLevel");
    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(
            buildRecord("INSERT", null, withoutAuthLevel), false));
  }

  private JsonNode buildRecord(String eventName, ObjectNode oldImage, ObjectNode newImage) {
    ObjectNode record = objectMapper.createObjectNode().put("eventName", eventName);
    ObjectNode dynamodb = record.putObject("dynamodb");
    if (oldImage != null)
      dynamodb.set("OldImage", oldImage);
    if (newImage != null)
      dynamodb.set("NewImage", newImage);
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
    image.set("clientErrorRedirectEnabled", boolValue(true));
    image.set("pairwise", stringValue("TOKEN"));
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
