package it.pagopa.oneid.web;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import it.pagopa.oneid.service.CacheUpdaterService;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Lambda bootstrap entrypoint for cache update events and DLQ retries.
 */
@ApplicationScoped
@Named("cache-updater")
public class CacheUpdaterHandler implements RequestHandler<JsonNode, Void> {

  private final CacheUpdaterService cacheUpdaterService;

  @Inject
  CacheUpdaterHandler(CacheUpdaterService cacheUpdaterService) {
    this.cacheUpdaterService = cacheUpdaterService;
  }

  @Override
  public Void handleRequest(JsonNode input, Context context) {
    Log.info("CacheUpdaterHandler invoked");
    cacheUpdaterService.processInput(input);
    return null;
  }
}
