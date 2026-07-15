package it.pagopa.oneid.common.utils.dynamodb;

import com.fasterxml.jackson.databind.JsonNode;
import it.pagopa.oneid.common.model.Client;
import java.util.Optional;

public interface DynamoStreamService {

  Optional<Client> extractClient(JsonNode streamRecord, boolean preferOldImage);

  Optional<String> extractClientId(JsonNode streamRecord, boolean preferOldImage);

  boolean hasCacheRelevantChanges(JsonNode streamRecord);
}
