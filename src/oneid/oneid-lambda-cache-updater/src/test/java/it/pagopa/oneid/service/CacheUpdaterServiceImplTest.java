package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.CacheConnector;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.utils.dynamodb.DynamoStreamService;
import it.pagopa.oneid.common.utils.dynamodb.RecordUtils;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;

@QuarkusTest
class CacheUpdaterServiceImplTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  CacheUpdaterService cacheUpdaterService;

  @InjectMock
  DynamoStreamService dynamoStreamService;

  @InjectMock
  RecordUtils recordUtils;

  @InjectMock
  CacheConnector cacheConnector;

  @InjectMock
  SnsClient sns;

  @Test
  @DisplayName("given insert record when process input then upsert client to cache")
  void given_insert_record_when_process_input_then_upsert_client_to_cache() {
    JsonNode input = buildArray("INSERT", "{\"clientId\":{\"S\":\"client-test\"}}",
      null);
    JsonNode streamRecord = input.get(0);
    Client client = Client.builder().clientId("client-test").build();
    when(recordUtils.readRecords(input)).thenReturn(List.of(streamRecord));
    when(dynamoStreamService.extractClient(streamRecord, false)).thenReturn(Optional.of(client));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(input));

    verify(cacheConnector).setClient(client);
  }

  @Test
  @DisplayName("given modify record without images when process input then skip and return OK")
  void given_modify_record_without_images_when_process_input_then_skip_and_return_ok() {
    JsonNode input = buildArray("MODIFY", null, null);
    JsonNode streamRecord = input.get(0);
    when(recordUtils.readRecords(input)).thenReturn(List.of(streamRecord));
    when(recordUtils.isIncompleteModifyRecord(streamRecord)).thenReturn(true);
    when(dynamoStreamService.extractClientId(streamRecord, false)).thenReturn(Optional.of("client-test"));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(input));

    verifyNoInteractions(cacheConnector);
  }

  @Test
  @DisplayName("given modify record without relevant changes when process input then skip and return OK")
  void given_modify_record_without_relevant_changes_when_process_input_then_skip_and_return_ok() {
    JsonNode input = buildArray("MODIFY",
        "{\"clientId\":{\"S\":\"client-test\"}}",
        "{\"clientId\":{\"S\":\"client-test\"}}"
    );
    JsonNode streamRecord = input.get(0);
    when(recordUtils.readRecords(input)).thenReturn(List.of(streamRecord));
    when(recordUtils.isIncompleteModifyRecord(streamRecord)).thenReturn(false);
    when(dynamoStreamService.hasCacheRelevantChanges(streamRecord)).thenReturn(false);
    when(dynamoStreamService.extractClientId(streamRecord, false))
        .thenReturn(Optional.of("client-test"));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(input));

    verifyNoInteractions(cacheConnector);
  }

  @Test
  @DisplayName("given modify record with relevant changes when process input then upsert to cache")
  void given_modify_record_with_relevant_changes_when_process_input_then_upsert_to_cache() {
    JsonNode input = buildArray("MODIFY",
        "{\"clientId\":{\"S\":\"client-test\"}}",
        "{\"clientId\":{\"S\":\"client-test\"},\"pairwise\":{\"BOOL\":true}}"
    );
    JsonNode streamRecord = input.get(0);
    Client client = Client.builder().clientId("client-test").build();
    when(recordUtils.readRecords(input)).thenReturn(List.of(streamRecord));
    when(recordUtils.isIncompleteModifyRecord(streamRecord)).thenReturn(false);
    when(dynamoStreamService.hasCacheRelevantChanges(streamRecord)).thenReturn(true);
    when(dynamoStreamService.extractClient(streamRecord, false)).thenReturn(Optional.of(client));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(input));

    verify(cacheConnector).setClient(client);
  }

  @Test
  @DisplayName("given remove record when process input then delete from cache")
  void given_remove_record_when_process_input_then_delete_from_cache() {
    JsonNode input = buildArray("REMOVE", "{\"clientId\":{\"S\":\"client-test\"}}", null);
    JsonNode streamRecord = input.get(0);
    when(recordUtils.readRecords(input)).thenReturn(List.of(streamRecord));
    when(dynamoStreamService.extractClientId(streamRecord, true))
        .thenReturn(Optional.of("client-test"));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(input));

    verify(cacheConnector).deleteClient("client-test");
  }

  @Test
  @DisplayName("given empty input when process input then log and return without touching cache")
  void given_empty_input_when_process_input_then_log_and_return_without_touching_cache() {
    JsonNode input = objectMapper.createArrayNode();
    when(recordUtils.readRecords(input)).thenReturn(List.of());

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(input));

    verifyNoInteractions(cacheConnector);
  }

  @Test
  @DisplayName("given invalid record when process input then throw illegal argument")
  void given_invalid_record_when_process_input_then_throw_illegal_argument() {
    JsonNode input = objectMapper.createArrayNode();
    when(recordUtils.readRecords(input)).thenReturn(List.of(objectMapper.nullNode()));

    assertThrows(IllegalArgumentException.class,
      () -> cacheUpdaterService.processInput(input));
  }

  @Test
  @DisplayName("given unsupported event name when process input then skip record")
  void given_unsupported_event_name_when_process_input_then_skip_record() {
    JsonNode input = buildArray("UNKNOWN", null, null);
    JsonNode streamRecord = input.get(0);
    when(recordUtils.readRecords(input)).thenReturn(List.of(streamRecord));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(input));

    verifyNoInteractions(cacheConnector, dynamoStreamService);
  }

  @Test
  @DisplayName("given insert record with blank client id when process input then throw")
  void given_insert_record_with_blank_client_id_when_process_input_then_throw() {
    JsonNode input = buildArray("INSERT", "{\"clientId\":{\"S\":\"client-test\"}}",
        "{\"clientId\":{\"S\":\"client-test\"}}"
    );
    JsonNode streamRecord = input.get(0);
    Client client = Client.builder().clientId(" ").build();
    when(recordUtils.readRecords(input)).thenReturn(List.of(streamRecord));
    when(dynamoStreamService.extractClient(streamRecord, false)).thenReturn(Optional.of(client));

    assertThrows(IllegalArgumentException.class,
      () -> cacheUpdaterService.processInput(input));
  }

  @Test
  @DisplayName("given remove record without client id when process input then throw")
  void given_remove_record_without_client_id_when_process_input_then_throw() {
    JsonNode input = buildArray("REMOVE", "{\"clientId\":{\"S\":\"client-test\"}}", null);
    JsonNode streamRecord = input.get(0);
    when(recordUtils.readRecords(input)).thenReturn(List.of(streamRecord));
    when(dynamoStreamService.extractClientId(streamRecord, true)).thenReturn(Optional.empty());

    assertThrows(IllegalStateException.class,
        () -> cacheUpdaterService.processInput(input));
  }

  private JsonNode buildArray(String eventName, String oldImageJson, String newImageJson) {
    try {
      ObjectNode streamRecord = objectMapper.createObjectNode();
      streamRecord.put("eventName", eventName);
      ObjectNode dynamodb = streamRecord.putObject("dynamodb");
      if (oldImageJson != null) {
        dynamodb.set("OldImage", objectMapper.readTree(oldImageJson));
      }
      if (newImageJson != null) {
        dynamodb.set("NewImage", objectMapper.readTree(newImageJson));
      }
      return objectMapper.createArrayNode().add(streamRecord);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
