package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.JsonNode;
import it.pagopa.oneid.common.model.Client;
import java.util.Optional;

public interface DynamoStreamService {

  Optional<Client> extractClient(JsonNode record, boolean preferOldImage);

  Optional<String> extractClientId(JsonNode record, boolean preferOldImage);

  boolean hasCacheRelevantChanges(JsonNode record);
}
