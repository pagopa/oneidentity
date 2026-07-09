package it.pagopa.oneid.service;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import it.pagopa.oneid.common.model.Client;
import java.util.Optional;

public interface DynamoStreamService {

  Optional<Client> extractClient(DynamodbStreamRecord streamRecord, boolean preferOldImage);

  Optional<String> extractClientId(DynamodbStreamRecord streamRecord, boolean preferOldImage);

  boolean hasCacheRelevantChanges(DynamodbStreamRecord streamRecord);
}
