package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.CacheConnector;
import it.pagopa.oneid.common.connector.ClientConnector;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class ClientLookupServiceImpl implements ClientLookupService {

  private final CacheConnector cacheConnector;
  private final ClientConnector clientConnector;
  private final CloudWatchConnectorImpl cloudWatchConnectorImpl;

  @Inject
  ClientLookupServiceImpl(
      CacheConnector cacheConnector,
      ClientConnector clientConnector,
      CloudWatchConnectorImpl cloudWatchConnectorImpl
  ) {
    this.cacheConnector = cacheConnector;
    this.clientConnector = clientConnector;
    this.cloudWatchConnectorImpl = cloudWatchConnectorImpl;
  }

  public Optional<Client> getClientById(String clientId) {
    if (clientId==null || clientId.isBlank()) {
      return Optional.empty();
    }

    Optional<Client> cachedClient = readFromCache(clientId);
    if (cachedClient.isPresent()) {
      Log.infof("Cache hit for clientId=%s", clientId);
      return cachedClient;
    }

    return fetchFromDynamo(clientId);
  }

  private Optional<Client> readFromCache(String clientId) {
    try {
      Log.debugf("Try to read clientId=%s from cache", clientId);
      return cacheConnector.getByClientId(clientId);
    } catch (RuntimeException cacheReadException) {
      Log.warnf(cacheReadException,
          "Unable to read client from Redis cache for clientId=%s. Falling back to DynamoDB.",
          clientId);
      return Optional.empty();
    }
  }

  private Optional<Client> fetchFromDynamo(String clientId) {

    Optional<Client> sourceClient = clientConnector.getClientById(clientId);
    if (sourceClient.isEmpty()) {
      return Optional.empty();
    }

    Client client = sourceClient.get();
    cloudWatchConnectorImpl.sendClientCacheMissMetricData(clientId);
    Log.infof("Cache miss for clientId=%s, fetched from DynamoDB", clientId);
    backfillCache(client);
    return sourceClient;
  }

  private void backfillCache(Client client) {
    try {
      cacheConnector.setClient(client);
      Log.infof("Cache backfill succeeded for clientId=%s", client.getClientId());
      cloudWatchConnectorImpl.sendClientCacheBackfillSuccessMetricData(client.getClientId());
    } catch (RuntimeException cacheWriteException) {
      Log.warnf(cacheWriteException,
          "Unable to write client to Redis cache for clientId=%s.", client.getClientId());
      cloudWatchConnectorImpl.sendClientCacheBackfillFailureMetricData(client.getClientId());
    }
  }
}
