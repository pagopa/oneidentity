package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import jakarta.inject.Inject;
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
  CacheConnector cacheConnector;

  @InjectMock
  SnsClient sns;

  @Test
  @DisplayName("given insert record when process input then upsert client to cache")
  void given_insert_record_when_process_input_then_upsert_client_to_cache() {
    Client client = Client.builder().clientId("client-test").build();
    when(dynamoStreamService.extractClient(any(), eq(false))).thenReturn(Optional.of(client));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(buildArray("INSERT",
        "{\"clientId\":{\"S\":\"client-test\"}}",
        null)));

    verify(cacheConnector).setClient(client);
  }

  @Test
  @DisplayName("given modify record without images when process input then skip and return OK")
  void given_modify_record_without_images_when_process_input_then_skip_and_return_ok() {
    assertDoesNotThrow(() -> cacheUpdaterService.processInput(buildArray("MODIFY", null, null)));

    verifyNoInteractions(cacheConnector);
  }

  @Test
  @DisplayName("given modify record without relevant changes when process input then skip and return OK")
  void given_modify_record_without_relevant_changes_when_process_input_then_skip_and_return_ok() {
    when(dynamoStreamService.hasCacheRelevantChanges(any())).thenReturn(false);
    when(dynamoStreamService.extractClientId(any(), eq(false)))
        .thenReturn(Optional.of("client-test"));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(buildArray("MODIFY",
        "{\"clientId\":{\"S\":\"client-test\"}}",
        "{\"clientId\":{\"S\":\"client-test\"}}")));

    verifyNoInteractions(cacheConnector);
  }

  @Test
  @DisplayName("given modify record with relevant changes when process input then upsert to cache")
  void given_modify_record_with_relevant_changes_when_process_input_then_upsert_to_cache() {
    Client client = Client.builder().clientId("client-test").build();
    when(dynamoStreamService.hasCacheRelevantChanges(any())).thenReturn(true);
    when(dynamoStreamService.extractClient(any(), eq(false))).thenReturn(Optional.of(client));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(buildArray("MODIFY",
        "{\"clientId\":{\"S\":\"client-test\"}}",
        "{\"clientId\":{\"S\":\"client-test\"},\"pairwise\":{\"BOOL\":true}}")));

    verify(cacheConnector).setClient(client);
  }

  @Test
  @DisplayName("given remove record when process input then delete from cache")
  void given_remove_record_when_process_input_then_delete_from_cache() {
    when(dynamoStreamService.extractClientId(any(), eq(true)))
        .thenReturn(Optional.of("client-test"));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(buildArray("REMOVE",
        "{\"clientId\":{\"S\":\"client-test\"}}",
        null)));

    verify(cacheConnector).deleteClient("client-test");
  }

  @Test
  @DisplayName("given empty input when process input then log and return without touching cache")
  void given_empty_input_when_process_input_then_log_and_return_without_touching_cache() {
    assertDoesNotThrow(() -> cacheUpdaterService.processInput(objectMapper.createArrayNode()));

    verifyNoInteractions(cacheConnector);
  }

  private JsonNode buildArray(String eventName, String oldImageJson, String newImageJson) {
    try {
      ObjectNode record = objectMapper.createObjectNode();
      record.put("eventName", eventName);
      ObjectNode dynamodb = record.putObject("dynamodb");
      if (oldImageJson != null) {
        dynamodb.set("OldImage", objectMapper.readTree(oldImageJson));
      }
      if (newImageJson != null) {
        dynamodb.set("NewImage", objectMapper.readTree(newImageJson));
      }
      return objectMapper.createArrayNode().add(record);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
