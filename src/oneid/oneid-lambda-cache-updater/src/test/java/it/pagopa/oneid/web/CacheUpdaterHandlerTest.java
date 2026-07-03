package it.pagopa.oneid.web;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.service.CacheUpdaterService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CacheUpdaterHandlerTest {

  @Inject
  CacheUpdaterHandler lambdaHandler;

  @Inject
  ObjectMapper objectMapper;

  @InjectMock
  CacheUpdaterService cacheUpdaterService;

  @Test
  @DisplayName("given null input when handle request then delegate to service and return null")
  void given_null_input_when_handle_request_then_delegate_to_service_and_return_null() {
    Void response = lambdaHandler.handleRequest(null, null);

    assertNull(response);
    verify(cacheUpdaterService).processInput(null);
  }

  @Test
  @DisplayName("given valid input when handle request then delegate to service and return null")
  void given_valid_input_when_handle_request_then_delegate_to_service_and_return_null() {
    Void response = handleRequest(
        "[{\"eventName\":\"INSERT\",\"dynamodb\":{\"NewImage\":{\"clientId\":{\"S\":\"client-test\"}}}}]");

    assertNull(response);
    verify(cacheUpdaterService).processInput(any());
  }

  @Test
  @DisplayName("given processing failure when handle request then rethrow exception")
  void given_processing_failure_when_handle_request_then_rethrow_exception() {
    doThrow(new IllegalStateException("processing failed"))
        .when(cacheUpdaterService).processInput(any(JsonNode.class));

    assertThrows(IllegalStateException.class,
        () -> handleRequest("{\"Records\":[{\"eventName\":\"INSERT\"}]}"));
  }

  private Void handleRequest(String payload) {
    try {
      return lambdaHandler.handleRequest(objectMapper.readTree(payload), null);
    } catch (JsonProcessingException jsonProcessingException) {
      throw new IllegalStateException("Unable to parse lambda handler payload",
          jsonProcessingException);
    }
  }
}
