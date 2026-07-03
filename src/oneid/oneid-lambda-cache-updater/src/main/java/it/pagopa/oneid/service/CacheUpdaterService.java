package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface CacheUpdaterService {
  void processInput(JsonNode input);
}
