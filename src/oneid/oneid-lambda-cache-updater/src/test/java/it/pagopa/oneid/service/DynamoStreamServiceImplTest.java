package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DynamoStreamServiceImplTest {

  @Inject
  DynamoStreamService dynamoStreamService;

  @Test
  @DisplayName("given new image when extracting client then build client from stream payload")
  void given_new_image_when_extracting_client_then_build_client_from_stream_payload() {
    DynamodbStreamRecord streamRecord = buildRecord("INSERT", null, baseImage());

    Optional<Client> result = dynamoStreamService.extractClient(streamRecord, false);

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
    DynamodbStreamRecord streamRecord = buildRecord("REMOVE", imageWithClientId("client-old"),
        null);

    Optional<String> clientId = dynamoStreamService.extractClientId(streamRecord, true);

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
    DynamodbStreamRecord streamRecord = buildRecord("REMOVE", imageWithClientId(" "), null);

    Optional<String> clientId = dynamoStreamService.extractClientId(streamRecord, true);

    assertTrue(clientId.isEmpty());
  }

  @Test
  @DisplayName("given modify stream with relevant changes when checking diff then return true")
  void given_modify_stream_with_relevant_changes_when_checking_diff_then_return_true() {
    Map<String, AttributeValue> oldImage = baseImage();
    Map<String, AttributeValue> newImage = new HashMap<>(baseImage());
    newImage.put("pairwise", new AttributeValue().withBOOL(false));

    DynamodbStreamRecord streamRecord = buildRecord("MODIFY", oldImage, newImage);

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(streamRecord));
  }

  @Test
  @DisplayName("given modify stream with only non relevant changes when checking diff then return false")
  void given_modify_stream_with_only_non_relevant_changes_when_checking_diff_then_return_false() {
    Map<String, AttributeValue> oldImage = new HashMap<>(baseImage());
    Map<String, AttributeValue> newImage = new HashMap<>(baseImage());
    oldImage.put("logoUri", new AttributeValue().withS("https://logo.old"));
    newImage.put("logoUri", new AttributeValue().withS("https://logo.new"));

    DynamodbStreamRecord streamRecord = buildRecord("MODIFY", oldImage, newImage);

    assertFalse(dynamoStreamService.hasCacheRelevantChanges(streamRecord));
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
  @DisplayName("given insert record with null-valued old image when extracting client from new image then succeed")
  void given_insert_record_with_pipe_injected_null_old_image_when_extracting_client_from_new_image_then_succeed() {
    Map<String, AttributeValue> nullOldImage = new HashMap<>();
    nullOldImage.put("clientId", new AttributeValue().withNULL(true));
    DynamodbStreamRecord streamRecord = buildRecord("INSERT", nullOldImage, baseImage());

    Optional<Client> result = dynamoStreamService.extractClient(streamRecord, false);

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
  }

  @Test
  @DisplayName("given empty image when extracting client then return empty")
  void given_empty_image_when_extracting_client_then_return_empty() {
    DynamodbStreamRecord streamRecord = buildRecord("INSERT", null, Map.of());

    Optional<Client> result = dynamoStreamService.extractClient(streamRecord, false);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("given rich dynamo payload when extracting client then handle all attribute types")
  void given_rich_dynamo_payload_when_extracting_client_then_handle_all_attribute_types() {
    Map<String, AttributeValue> image = new HashMap<>(baseImage());
    image.put("rawList", new AttributeValue().withL(
        List.of(new AttributeValue().withS("nested"), new AttributeValue().withN("9"))
    ));
    Map<String, AttributeValue> nestedMap = new HashMap<>();
    nestedMap.put("nestedText", new AttributeValue().withS("nested"));
    nestedMap.put("nestedNumber", new AttributeValue().withN("9"));
    image.put("rawMap", new AttributeValue().withM(nestedMap));
    image.put("rawNull", new AttributeValue().withNULL(true));

    DynamodbStreamRecord streamRecord = buildRecord("INSERT", null, image);

    Optional<Client> result = dynamoStreamService.extractClient(streamRecord, false);

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
  }

  @Test
  @DisplayName("given invalid auth level when extracting client then throw")
  void given_invalid_auth_level_when_extracting_client_then_throw() {
    Map<String, AttributeValue> image = new HashMap<>(baseImage());
    image.put("authLevel", new AttributeValue().withS("invalid-auth-level"));

    DynamodbStreamRecord streamRecord = buildRecord("INSERT", null, image);

    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(streamRecord, false));
  }

  @Test
  @DisplayName("given invalid saml binding when extracting client then default to HTTP_POST")
  void given_invalid_saml_binding_when_extracting_client_then_default_to_http_post() {
    Map<String, AttributeValue> image = new HashMap<>(baseImage());
    image.put("samlBinding", new AttributeValue().withS("invalid-saml-binding"));

    DynamodbStreamRecord streamRecord = buildRecord("INSERT", null, image);

    Optional<Client> result = dynamoStreamService.extractClient(streamRecord, false);

    assertTrue(result.isPresent());
    assertEquals(SamlBinding.HTTP_POST, result.get().getSamlBinding());
  }

  @Test
  @DisplayName("given blank client id when extracting client then throw")
  void given_blank_client_id_when_extracting_client_then_throw() {
    Map<String, AttributeValue> image = new HashMap<>(baseImage());
    image.put("clientId", new AttributeValue().withS(" "));

    DynamodbStreamRecord streamRecord = buildRecord("INSERT", null, image);

    assertThrows(IllegalArgumentException.class,
        () -> dynamoStreamService.extractClient(streamRecord, false));
  }

  @Test
  @DisplayName("given modify stream with only friendly name change when checking diff then return true")
  void given_modify_stream_with_only_friendly_name_change_when_checking_diff_then_return_true() {
    Map<String, AttributeValue> oldImage = new HashMap<>(baseImage());
    Map<String, AttributeValue> newImage = new HashMap<>(baseImage());
    oldImage.put("friendlyName", new AttributeValue().withS("Old Name"));
    newImage.put("friendlyName", new AttributeValue().withS("New Name"));

    DynamodbStreamRecord streamRecord = buildRecord("MODIFY", oldImage, newImage);

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(streamRecord));
  }

  private DynamodbStreamRecord buildRecord(String eventName,
                                            Map<String, AttributeValue> oldImage,
                                            Map<String, AttributeValue> newImage) {
    DynamodbStreamRecord record = new DynamodbStreamRecord();
    record.setEventName(eventName);
    StreamRecord streamRecord = new StreamRecord();
    if (oldImage != null) {
      streamRecord.setOldImage(oldImage);
    }
    if (newImage != null) {
      streamRecord.setNewImage(newImage);
    }
    record.setDynamodb(streamRecord);
    return record;
  }

  private DynamodbStreamRecord buildRecordWithoutDynamodb(String eventName) {
    DynamodbStreamRecord record = new DynamodbStreamRecord();
    record.setEventName(eventName);
    return record;
  }

  private Map<String, AttributeValue> baseImage() {
    Map<String, AttributeValue> image = new HashMap<>();
    image.put("clientId", new AttributeValue().withS("client-test"));
    image.put("callbackURI",
        new AttributeValue().withSS(List.of("https://callback.example")));
    image.put("requestedParameters",
        new AttributeValue().withSS(List.of("fiscalNumber")));
    image.put("authLevel",
        new AttributeValue().withS("https://www.spid.gov.it/SpidL2"));
    image.put("samlBinding",
        new AttributeValue().withS("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"));
    image.put("acsIndex", new AttributeValue().withN("1"));
    image.put("attributeIndex", new AttributeValue().withN("2"));
    image.put("active", new AttributeValue().withBOOL(true));
    image.put("requiredSameIdp", new AttributeValue().withBOOL(true));
    image.put("pairwise", new AttributeValue().withBOOL(true));
    image.put("spidMinors", new AttributeValue().withBOOL(false));
    image.put("spidProfessionals", new AttributeValue().withBOOL(false));
    image.put("minAge", new AttributeValue().withN("0"));
    image.put("maxAge", new AttributeValue().withN("0"));
    image.put("ageParentAuth", new AttributeValue().withN("0"));
    return image;
  }

  private Map<String, AttributeValue> imageWithClientId(String clientId) {
    Map<String, AttributeValue> image = new HashMap<>();
    image.put("clientId", new AttributeValue().withS(clientId));
    return image;
  }
}
