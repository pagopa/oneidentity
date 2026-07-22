package it.pagopa.oneid.web;

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import it.pagopa.oneid.service.ClientPublisherService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ClientPublisherHandlerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  ClientPublisherHandler handler;

  @InjectMock
  ClientPublisherService service;

  @Test
  @DisplayName("ordinary event continues through stream processing")
  void handleRequest_ordinaryEventProcessesInput() throws Exception {
    var input = objectMapper.readTree("[{\"eventName\":\"INSERT\"}]");

    handler.handleRequest(input, null);

    verify(service).processInput(input);
  }

  @Test
  @DisplayName("null and empty events are treated as ordinary input")
  void handleRequest_nullAndEmptyEventsProcessInput() throws Exception {
    handler.handleRequest(null, null);
    handler.handleRequest(objectMapper.readTree("[]"), null);

    verify(service).processInput((JsonNode) null);
    verify(service).processInput(objectMapper.readTree("[]"));
  }
}
