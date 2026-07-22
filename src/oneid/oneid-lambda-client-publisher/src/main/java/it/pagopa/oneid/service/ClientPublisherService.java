package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface ClientPublisherService {
  void processInput(JsonNode input);
}
