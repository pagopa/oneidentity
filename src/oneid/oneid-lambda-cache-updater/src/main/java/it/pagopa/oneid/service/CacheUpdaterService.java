package it.pagopa.oneid.service;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import java.util.List;

public interface CacheUpdaterService {
  void processInput(List<DynamodbStreamRecord> records);
}
