package it.pagopa.oneid.web;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.service.CacheUpdaterService;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CacheUpdaterHandlerTest {

  @Inject
  CacheUpdaterHandler lambdaHandler;

  @InjectMock
  CacheUpdaterService cacheUpdaterService;

  @Test
  @DisplayName("given null input when handle request then delegate to service and return null")
  void given_null_input_when_handle_request_then_delegate_to_service_and_return_null() {
    Void response = lambdaHandler.handleRequest(null, null);

    assertNull(response);
    verify(cacheUpdaterService).processInput(List.of());
  }

  @Test
  @DisplayName("given valid input when handle request then delegate to service and return null")
  void given_valid_input_when_handle_request_then_delegate_to_service_and_return_null() {
    List<DynamodbStreamRecord> records = List.of(buildInsertRecord("client-test"));

    Void response = lambdaHandler.handleRequest(records, null);

    assertNull(response);
    verify(cacheUpdaterService).processInput(records);
  }

  @Test
  @DisplayName("given processing failure when handle request then rethrow exception")
  void given_processing_failure_when_handle_request_then_rethrow_exception() {
    doThrow(new IllegalStateException("processing failed"))
        .when(cacheUpdaterService).processInput(any());

    assertThrows(IllegalStateException.class,
        () -> lambdaHandler.handleRequest(List.of(buildInsertRecord("client-test")), null));
  }

  private DynamodbStreamRecord buildInsertRecord(String clientId) {
    DynamodbStreamRecord record = new DynamodbStreamRecord();
    record.setEventName("INSERT");
    StreamRecord streamRecord = new StreamRecord();
    streamRecord.setNewImage(Map.of("clientId", new AttributeValue().withS(clientId)));
    record.setDynamodb(streamRecord);
    return record;
  }
}
