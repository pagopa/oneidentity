package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.CacheConnector;
import it.pagopa.oneid.common.model.Client;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;

@QuarkusTest
class CacheUpdaterServiceImplTest {

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
    DynamodbStreamRecord streamRecord = buildRecord("INSERT", null,
        Map.of("clientId", new AttributeValue().withS("client-test")));
    Client client = Client.builder().clientId("client-test").build();
    when(dynamoStreamService.extractClient(streamRecord, false)).thenReturn(Optional.of(client));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(List.of(streamRecord)));

    verify(cacheConnector).setClient(client);
  }

  @Test
  @DisplayName("given modify record without images when process input then skip and return OK")
  void given_modify_record_without_images_when_process_input_then_skip_and_return_ok() {
    DynamodbStreamRecord streamRecord = buildRecord("MODIFY", null, null);
    when(dynamoStreamService.extractClientId(streamRecord, false)).thenReturn(
        Optional.of("client-test"));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(List.of(streamRecord)));

    verifyNoInteractions(cacheConnector);
  }

  @Test
  @DisplayName("given modify record without relevant changes when process input then skip and return OK")
  void given_modify_record_without_relevant_changes_when_process_input_then_skip_and_return_ok() {
    Map<String, AttributeValue> image = Map.of("clientId",
        new AttributeValue().withS("client-test"));
    DynamodbStreamRecord streamRecord = buildRecord("MODIFY", image, image);
    when(dynamoStreamService.hasCacheRelevantChanges(streamRecord)).thenReturn(false);
    when(dynamoStreamService.extractClientId(streamRecord, false)).thenReturn(
        Optional.of("client-test"));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(List.of(streamRecord)));

    verifyNoInteractions(cacheConnector);
  }

  @Test
  @DisplayName("given modify record with relevant changes when process input then upsert to cache")
  void given_modify_record_with_relevant_changes_when_process_input_then_upsert_to_cache() {
    Map<String, AttributeValue> image = Map.of("clientId",
        new AttributeValue().withS("client-test"));
    DynamodbStreamRecord streamRecord = buildRecord("MODIFY", image, image);
    Client client = Client.builder().clientId("client-test").build();
    when(dynamoStreamService.hasCacheRelevantChanges(streamRecord)).thenReturn(true);
    when(dynamoStreamService.extractClient(streamRecord, false)).thenReturn(Optional.of(client));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(List.of(streamRecord)));

    verify(cacheConnector).setClient(client);
  }

  @Test
  @DisplayName("given remove record when process input then delete from cache")
  void given_remove_record_when_process_input_then_delete_from_cache() {
    Map<String, AttributeValue> image = Map.of("clientId",
        new AttributeValue().withS("client-test"));
    DynamodbStreamRecord streamRecord = buildRecord("REMOVE", image, null);
    when(dynamoStreamService.extractClientId(streamRecord, true)).thenReturn(
        Optional.of("client-test"));

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(List.of(streamRecord)));

    verify(cacheConnector).deleteClient("client-test");
  }

  @Test
  @DisplayName("given empty input when process input then log and return without touching cache")
  void given_empty_input_when_process_input_then_log_and_return_without_touching_cache() {
    assertDoesNotThrow(() -> cacheUpdaterService.processInput(List.of()));

    verifyNoInteractions(cacheConnector);
  }

  @Test
  @DisplayName("given null record when process input then throw illegal argument")
  void given_null_record_when_process_input_then_throw_illegal_argument() {
    assertThrows(IllegalArgumentException.class,
        () -> cacheUpdaterService.processInput(Collections.singletonList(null)));
  }

  @Test
  @DisplayName("given unsupported event name when process input then skip record")
  void given_unsupported_event_name_when_process_input_then_skip_record() {
    DynamodbStreamRecord streamRecord = new DynamodbStreamRecord();
    streamRecord.setEventName("UNKNOWN");

    assertDoesNotThrow(() -> cacheUpdaterService.processInput(List.of(streamRecord)));

    verifyNoInteractions(cacheConnector, dynamoStreamService);
  }

  @Test
  @DisplayName("given insert record with blank client id when process input then throw")
  void given_insert_record_with_blank_client_id_when_process_input_then_throw() {
    DynamodbStreamRecord streamRecord = buildRecord("INSERT", null,
        Map.of("clientId", new AttributeValue().withS("client-test")));
    Client client = Client.builder().clientId(" ").build();
    when(dynamoStreamService.extractClient(streamRecord, false)).thenReturn(Optional.of(client));

    assertThrows(IllegalArgumentException.class,
        () -> cacheUpdaterService.processInput(List.of(streamRecord)));
  }

  @Test
  @DisplayName("given remove record without client id when process input then throw")
  void given_remove_record_without_client_id_when_process_input_then_throw() {
    Map<String, AttributeValue> image = Map.of("clientId",
        new AttributeValue().withS("client-test"));
    DynamodbStreamRecord streamRecord = buildRecord("REMOVE", image, null);
    when(dynamoStreamService.extractClientId(streamRecord, true)).thenReturn(Optional.empty());

    assertThrows(IllegalStateException.class,
        () -> cacheUpdaterService.processInput(List.of(streamRecord)));
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
}
