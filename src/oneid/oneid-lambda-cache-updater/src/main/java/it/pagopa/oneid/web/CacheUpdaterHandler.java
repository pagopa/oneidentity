package it.pagopa.oneid.web;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import it.pagopa.oneid.service.CacheUpdaterService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

/**
 * Lambda bootstrap entrypoint for cache update events from DynamoDB Streams.
 */
@ApplicationScoped
@Named("cache-updater")
public class CacheUpdaterHandler implements RequestHandler<List<DynamodbStreamRecord>, Void> {

  private final CacheUpdaterService cacheUpdaterService;

  @Inject
  CacheUpdaterHandler(CacheUpdaterService cacheUpdaterService) {
    this.cacheUpdaterService = cacheUpdaterService;
  }

  @Override
  public Void handleRequest(List<DynamodbStreamRecord> input, Context context) {
    Log.info("CacheUpdaterHandler invoked");
    List<DynamodbStreamRecord> records = input == null ? List.of() : input;
    Log.infof("CacheUpdaterHandler received %d DynamoDB record(s): %s", records.size(), records);
    cacheUpdaterService.processInput(records);
    return null;
  }
}
