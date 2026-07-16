package it.pagopa.oneid.web;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import it.pagopa.oneid.service.ClientPublisherService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class ClientPublisherHandlerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  ClientPublisherHandler handler;

  @InjectMock
  ClientPublisherService service;

  @Test
  @DisplayName("bootstrap event starts self-bootstrap and skips stream processing")
  void handleRequest_bootstrapEventRunsSelfBootstrap() throws Exception {
    handler.handleRequest(objectMapper.readTree("[{\"action\":\"bootstrap\"}]"), null);

    verify(service).runSelfBootstrap();
    verify(service, never()).processInput(Mockito.any());
  }

  @Test
  @DisplayName("ordinary event continues through stream processing")
  void handleRequest_ordinaryEventProcessesInput() throws Exception {
    var input = objectMapper.readTree("[{\"eventName\":\"INSERT\"}]");

    handler.handleRequest(input, null);

    verify(service).processInput(input);
    verify(service, never()).runSelfBootstrap();
  }

  @Test
  @DisplayName("null and empty events are treated as ordinary input")
  void handleRequest_nullAndEmptyEventsProcessInput() throws Exception {
    handler.handleRequest(null, null);
    handler.handleRequest(objectMapper.readTree("[]"), null);

    verify(service).processInput((JsonNode) null);
    verify(service).processInput(objectMapper.readTree("[]"));
    verify(service, never()).runSelfBootstrap();
  }
}
