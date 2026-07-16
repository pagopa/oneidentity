package it.pagopa.oneid.web;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.logging.Log;
import it.pagopa.oneid.service.ClientPublisherService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Lambda bootstrap entrypoint for client registration events from DynamoDB Streams.
 */
@ApplicationScoped
@Named("client-publisher")
public class ClientPublisherHandler implements RequestHandler<JsonNode, Void> {

  private final ClientPublisherService clientPublisherService;

  @Inject
  ClientPublisherHandler(ClientPublisherService clientPublisherService) {
    this.clientPublisherService = clientPublisherService;
  }

  @Override
  public Void handleRequest(JsonNode input, Context context) {
    Log.info("ClientPublisherHandler invoked");
    if (isBootstrapEvent(input)) {
      Log.info("Starting client publisher self-bootstrap");
      clientPublisherService.runSelfBootstrap();
      return null;
    }
    clientPublisherService.processInput(input);
    return null;
  }

  private boolean isBootstrapEvent(JsonNode input) {
    return input != null
        && input.isArray()
        && !input.isEmpty()
        && "bootstrap".equals(input.get(0).path("action").asText());
  }
}
