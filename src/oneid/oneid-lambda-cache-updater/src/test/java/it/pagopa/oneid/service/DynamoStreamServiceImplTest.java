package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DynamoStreamServiceImplTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  DynamoStreamService dynamoStreamService;

  @Test
  @DisplayName("given new image when extracting client then build client from stream payload")
  void given_new_image_when_extracting_client_then_build_client_from_stream_payload() {
    JsonNode record = buildRecord("INSERT", null, baseImage());

    Optional<Client> result = dynamoStreamService.extractClient(record, false);

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
    assertEquals(Set.of("https://callback.example"), result.get().getCallbackURI());
    assertEquals(Set.of("fiscalNumber"), result.get().getRequestedParameters());
    assertEquals(AuthLevel.L2, result.get().getAuthLevel());
    assertEquals(SamlBinding.HTTP_POST, result.get().getSamlBinding());
    assertEquals(1, result.get().getAcsIndex());
    assertEquals(2, result.get().getAttributeIndex());
    assertTrue(result.get().isActive());
    assertTrue(result.get().isPairwise());
  }

  @Test
  @DisplayName("given old image when extracting client id then return id from old image")
  void given_old_image_when_extracting_client_id_then_return_id_from_old_image() {
    JsonNode record = buildRecord("REMOVE", imageWithClientId("client-old"), null);

    Optional<String> clientId = dynamoStreamService.extractClientId(record, true);

    assertTrue(clientId.isPresent());
    assertEquals("client-old", clientId.get());
  }

  @Test
  @DisplayName("given null record when extracting client id then return empty")
  void given_null_record_when_extracting_client_id_then_return_empty() {
    Optional<String> clientId = dynamoStreamService.extractClientId(null, false);

    assertTrue(clientId.isEmpty());
  }

  @Test
  @DisplayName("given blank client id when extracting client id then return empty")
  void given_blank_client_id_when_extracting_client_id_then_return_empty() {
    JsonNode record = buildRecord("REMOVE", imageWithClientId(" "), null);

    Optional<String> clientId = dynamoStreamService.extractClientId(record, true);

    assertTrue(clientId.isEmpty());
  }

  @Test
  @DisplayName("given modify stream with relevant changes when checking diff then return true")
  void given_modify_stream_with_relevant_changes_when_checking_diff_then_return_true() {
    ObjectNode oldImage = baseImage();
    ObjectNode newImage = baseImage();
    newImage.set("pairwise", boolValue(false));

    JsonNode record = buildRecord("MODIFY", oldImage, newImage);

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(record));
  }

  @Test
  @DisplayName("given modify stream with only non relevant changes when checking diff then return false")
  void given_modify_stream_with_only_non_relevant_changes_when_checking_diff_then_return_false() {
    ObjectNode oldImage = baseImage();
    ObjectNode newImage = baseImage();
    oldImage.set("logoUri", stringValue("https://logo.old"));
    newImage.set("logoUri", stringValue("https://logo.new"));

    JsonNode record = buildRecord("MODIFY", oldImage, newImage);

    assertFalse(dynamoStreamService.hasCacheRelevantChanges(record));
  }

  @Test
  @DisplayName("given null record when checking diff then return false")
  void given_null_record_when_checking_diff_then_return_false() {
    assertFalse(dynamoStreamService.hasCacheRelevantChanges(null));
  }

  @Test
  @DisplayName("given record without dynamodb when checking diff then return false")
  void given_record_without_dynamodb_when_checking_diff_then_return_false() {
    assertFalse(dynamoStreamService.hasCacheRelevantChanges(buildRecordWithoutDynamodb("MODIFY")));
  }

  @Test
  @DisplayName("given insert record with pipe-injected null old image when extracting client from new image then succeed")
  void given_insert_record_with_pipe_injected_null_old_image_when_extracting_client_from_new_image_then_succeed() {
    JsonNode record = buildRecord("INSERT", buildNullOldImage(), baseImage());

    Optional<Client> result = dynamoStreamService.extractClient(record, false);

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
  }

  @Test
  @DisplayName("given empty image when extracting client then return empty")
  void given_empty_image_when_extracting_client_then_return_empty() {
    JsonNode record = buildRecord("INSERT", null, objectMapper.createObjectNode());

    Optional<Client> result = dynamoStreamService.extractClient(record, false);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("given rich dynamo payload when extracting client then ignore extra attribute shapes")
  void given_rich_dynamo_payload_when_extracting_client_then_ignore_extra_attribute_shapes() {
    ObjectNode image = baseImage();
    image.put("rawText", "text");
    image.put("rawNumber", 7);
    image.put("rawBoolean", false);

    ObjectNode rawList = objectMapper.createObjectNode();
    ArrayNode listValues = rawList.putArray("L");
    listValues.addObject().put("S", "nested");
    listValues.addObject().put("N", "9");
    image.set("rawList", rawList);

    ObjectNode rawMap = objectMapper.createObjectNode();
    ObjectNode mapValues = objectMapper.createObjectNode();
    mapValues.putObject("nestedText").put("S", "nested");
    mapValues.putObject("nestedNumber").put("N", "9");
    rawMap.set("M", mapValues);
    image.set("rawMap", rawMap);

    ObjectNode rawNull = objectMapper.createObjectNode();
    rawNull.put("NULL", true);
    image.set("rawNull", rawNull);

    image.set("rawUnhandled", objectMapper.createObjectNode());

    JsonNode record = buildRecord("INSERT", null, image);

    Optional<Client> result = dynamoStreamService.extractClient(record, false);

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
  }

  @Test
  @DisplayName("given invalid auth level when extracting client then throw")
  void given_invalid_auth_level_when_extracting_client_then_throw() {
    ObjectNode image = baseImage();
    image.set("authLevel", stringValue("invalid-auth-level"));

    JsonNode record = buildRecord("INSERT", null, image);

    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(record, false));
  }

  @Test
  @DisplayName("given invalid saml binding when extracting client then default to HTTP_POST")
  void given_invalid_saml_binding_when_extracting_client_then_default_to_http_post() {
    ObjectNode image = baseImage();
    image.set("samlBinding", stringValue("invalid-saml-binding"));

    JsonNode record = buildRecord("INSERT", null, image);

    Optional<Client> result = dynamoStreamService.extractClient(record, false);

    assertTrue(result.isPresent());
    assertEquals(SamlBinding.HTTP_POST, result.get().getSamlBinding());
  }

  @Test
  @DisplayName("given blank client id when extracting client then throw")
  void given_blank_client_id_when_extracting_client_then_throw() {
    ObjectNode image = baseImage();
    image.set("clientId", stringValue(" "));

    JsonNode record = buildRecord("INSERT", null, image);

    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(record, false));
  }

  @Test
  @DisplayName("given modify stream with only friendly name change when checking diff then return true")
  void given_modify_stream_with_only_friendly_name_change_when_checking_diff_then_return_true() {
    ObjectNode oldImage = baseImage();
    ObjectNode newImage = baseImage();
    oldImage.set("friendlyName", stringValue("Old Name"));
    newImage.set("friendlyName", stringValue("New Name"));

    JsonNode record = buildRecord("MODIFY", oldImage, newImage);

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(record));
  }

  private JsonNode buildRecord(String eventName, ObjectNode oldImage, ObjectNode newImage) {
    ObjectNode record = objectMapper.createObjectNode();
    record.put("eventName", eventName);

    ObjectNode dynamodb = record.putObject("dynamodb");
    if (oldImage != null) {
      dynamodb.set("OldImage", oldImage);
    }
    if (newImage != null) {
      dynamodb.set("NewImage", newImage);
    }

    return record;
  }

  private JsonNode buildRecordWithoutDynamodb(String eventName) {
    ObjectNode record = objectMapper.createObjectNode();
    record.put("eventName", eventName);
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

  private ObjectNode imageWithClientId(String clientId) {
    ObjectNode image = objectMapper.createObjectNode();
    image.set("clientId", stringValue(clientId));
    return image;
  }

  private ObjectNode stringValue(String value) {
    ObjectNode attributeValue = objectMapper.createObjectNode();
    attributeValue.put("S", value);
    return attributeValue;
  }

  private ObjectNode numberValue(long value) {
    ObjectNode attributeValue = objectMapper.createObjectNode();
    attributeValue.put("N", String.valueOf(value));
    return attributeValue;
  }

  private ObjectNode boolValue(boolean value) {
    ObjectNode attributeValue = objectMapper.createObjectNode();
    attributeValue.put("BOOL", value);
    return attributeValue;
  }

  private ObjectNode stringSetValue(String value) {
    ObjectNode attributeValue = objectMapper.createObjectNode();
    attributeValue.putArray("SS").add(value);
    return attributeValue;
  }

  private ObjectNode buildNullOldImage() {
    ObjectNode image = objectMapper.createObjectNode();
    image.putNull("clientId");
    image.putNull("authLevel");
    image.putNull("samlBinding");
    image.putNull("acsIndex");
    image.putNull("attributeIndex");
    image.putNull("active");
    image.putNull("requiredSameIdp");
    image.putNull("pairwise");
    image.putNull("spidMinors");
    image.putNull("spidProfessionals");
    image.putNull("callbackURI");
    image.putNull("requestedParameters");
    image.putNull("minAge");
    image.putNull("maxAge");
    image.putNull("ageParentAuth");
    return image;
  }
}
