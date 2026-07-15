package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.CacheConnector;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.utils.dynamodb.DynamoStreamService;
import it.pagopa.oneid.common.utils.dynamodb.RecordUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sns.SnsClient;

@ApplicationScoped
public class CacheUpdaterServiceImpl implements CacheUpdaterService {

  private final CacheConnector cacheConnector;
  private final DynamoStreamService dynamoStreamService;
  private final RecordUtils recordUtils;
  private final SnsClient sns;
  private final String snsTopicArn;

  @Inject
  CacheUpdaterServiceImpl(
      CacheConnector cacheConnector,
      DynamoStreamService dynamoStreamService,
      RecordUtils recordUtils,
      SnsClient sns,
      @ConfigProperty(name = "sns_topic_arn") String snsTopicArn) {
    this.cacheConnector = cacheConnector;
    this.dynamoStreamService = dynamoStreamService;
    this.recordUtils = recordUtils;
    this.sns = sns;
    this.snsTopicArn = snsTopicArn;
  }

  @Override
  public void processInput(JsonNode input) {
    List<JsonNode> records = recordUtils.readRecords(input);
    if (records.isEmpty()) {
      Log.info("No DynamoDB records to process");
      return;
    }

    for (JsonNode streamRecord : records) {
      try {
        processRecord(streamRecord);
      } catch (RuntimeException processingException) {
        Log.error("Cache update from DynamoDB stream failed. Record will be retried and may reach "
            + "DLQ after max attempts.", processingException);
        throw processingException;
      }
    }
  }

  private void processRecord(JsonNode streamRecord) {
    if (streamRecord == null || streamRecord.isNull() || !streamRecord.isObject()) {
      throw new IllegalArgumentException("Invalid DynamoDB stream record");
    }

    String eventName = streamRecord.path("eventName").asText();
    if (eventName.isBlank()) {
      throw new IllegalArgumentException("DynamoDB stream record without eventName");
    }

    switch (eventName) {
      case "INSERT" -> dynamoStreamService.extractClient(streamRecord, false)
          .ifPresentOrElse(this::upsertClient,
              () -> {
                throw new IllegalStateException(
                    "Unable to build client from NEW_IMAGE for eventName=" + eventName);
              });
      case "MODIFY" -> {
        if (recordUtils.isIncompleteModifyRecord(streamRecord)) {
          String clientId = dynamoStreamService.extractClientId(streamRecord, false)
              .orElse("unknown");
          Log.warnf("Skipping MODIFY for clientId=%s because stream images are incomplete",
              clientId);
          return;
        }

        if (!dynamoStreamService.hasCacheRelevantChanges(streamRecord)) {
          String clientId = dynamoStreamService.extractClientId(streamRecord, false)
              .orElse("unknown");
          Log.infof("Skipping cache update for clientId=%s because no cache-relevant fields "
              + "changed", clientId);
          return;
        }

        dynamoStreamService.extractClient(streamRecord, false)
            .ifPresentOrElse(this::upsertClient,
                () -> {
                  throw new IllegalStateException(
                      "Unable to build client from NEW_IMAGE for eventName=" + eventName);
                });
      }
      case "REMOVE" -> dynamoStreamService.extractClientId(streamRecord, true)
          .ifPresentOrElse(this::deleteClient,
              () -> {
                throw new IllegalStateException(
                    "Unable to read clientId from OLD_IMAGE for REMOVE event");
              });
      default -> Log.infof("Unsupported DynamoDB stream eventName=%s", eventName);
    }
  }

  private void upsertClient(Client client) {
    if (client == null || StringUtils.isBlank(client.getClientId())) {
      throw new IllegalArgumentException("client must include clientId");
    }
    cacheConnector.setClient(client);
    Log.infof("Cache upsert completed for clientId=%s", client.getClientId());
    publishNotification("Cache updated", client.getClientId(), "UPSERT");
  }

  private void deleteClient(String clientId) {
    cacheConnector.deleteClient(clientId);
    Log.infof("Cache delete completed for clientId=%s", clientId);
    publishNotification("Cache updated", clientId, "DELETE");
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
