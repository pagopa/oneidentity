package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.CacheConnector;
import it.pagopa.oneid.common.connector.ClientConnector;
import it.pagopa.oneid.common.model.Client;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sns.SnsClient;

@ApplicationScoped
public class ClientLookupServiceImpl implements ClientLookupService {

  private final CacheConnector cacheConnector;
  private final ClientConnector clientConnector;
  private final SnsClient sns;
  private final String snsTopicArn;

  @Inject
  ClientLookupServiceImpl(
      CacheConnector cacheConnector,
      ClientConnector clientConnector,
      SnsClient sns,
      @ConfigProperty(name = "sns_topic_arn_cache") String snsTopicArn
  ) {
    this.cacheConnector = cacheConnector;
    this.clientConnector = clientConnector;
    this.sns = sns;
    this.snsTopicArn = snsTopicArn;
  }

  public Optional<Client> getClientById(String clientId) {
    if (clientId==null || clientId.isBlank()) {
      return Optional.empty();
    }

    Optional<Client> cachedClient = readFromCache(clientId);
    if (cachedClient.isPresent()) {
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
    Log.infof("Cache miss for clientId=%s, fetched from DynamoDB", clientId);
    publishNotification("Cache miss: client fetched from DynamoDB", clientId, "DYNAMO_FALLBACK_READ");
    backfillCache(client);
    return sourceClient;
  }

  private void backfillCache(Client client) {
    try {
      cacheConnector.setClient(client);
      Log.infof("Cache backfill succeeded for clientId=%s", client.getClientId());
      publishNotification("Cache backfill succeeded", client.getClientId(), "CACHE_BACKFILL_SUCCESS");
    } catch (RuntimeException cacheWriteException) {
      Log.warnf(cacheWriteException,
          "Unable to write client to Redis cache for clientId=%s.", client.getClientId());
      publishNotification("Cache backfill failed", client.getClientId(), "CACHE_BACKFILL_FAILURE");
    }
  }

  private void publishNotification(String subject, String clientId, String action) {
    String message = "Action: " + action + "\nClient ID: " + clientId;
    try {
      sns.publish(p -> p.topicArn(snsTopicArn).subject(subject).message(message));
      Log.debugf("SNS notification sent for clientId=%s action=%s", clientId, action);
    } catch (Exception e) {
      Log.warnf(e, "Failed to send SNS notification for clientId=%s action=%s", clientId, action);
    }
  }
}
