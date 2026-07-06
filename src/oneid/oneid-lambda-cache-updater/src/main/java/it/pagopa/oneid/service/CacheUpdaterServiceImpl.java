package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.CacheConnector;
import it.pagopa.oneid.common.model.Client;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sns.SnsClient;

@ApplicationScoped
public class CacheUpdaterServiceImpl implements CacheUpdaterService {

  @Inject
  CacheConnector cacheConnector;

  @Inject
  DynamoStreamService dynamoStreamService;

  @Inject
  RecordUtils recordUtils;

  @Inject
  SnsClient sns;

  @ConfigProperty(name = "sns_topic_arn")
  String snsTopicArn;

  @Override
  public void processInput(JsonNode input) {
    List<JsonNode> records = recordUtils.readRecords(input);
    if (records.isEmpty()) {
      Log.info("No DynamoDB records to process");
      return;
    }

    for (JsonNode record : records) {
      try {
        processRecord(record);
      } catch (RuntimeException processingException) {
        Log.error("Cache update from DynamoDB stream failed. Record will be retried and may reach "
            + "DLQ after max attempts.", processingException);
        throw processingException;
      }
    }
  }

  private void processRecord(JsonNode record) {
    if (record == null || record.isNull() || !record.isObject()) {
      throw new IllegalArgumentException("Invalid DynamoDB stream record");
    }

    String eventName = record.path("eventName").asText();
    if (eventName.isBlank()) {
      throw new IllegalArgumentException("DynamoDB stream record without eventName");
    }

    switch (eventName) {
      case "INSERT" -> dynamoStreamService.extractClient(record, false)
          .ifPresentOrElse(this::upsertClient,
              () -> {
                throw new IllegalStateException(
                    "Unable to build client from NEW_IMAGE for eventName=" + eventName);
              });
      case "MODIFY" -> {
        if (recordUtils.isIncompleteModifyRecord(record)) {
          String clientId = dynamoStreamService.extractClientId(record, false).orElse("unknown");
          Log.warnf("Skipping MODIFY for clientId=%s because stream images are incomplete",
              clientId);
          return;
        }

        if (!dynamoStreamService.hasCacheRelevantChanges(record)) {
          String clientId = dynamoStreamService.extractClientId(record, false).orElse("unknown");
          Log.infof("Skipping cache update for clientId=%s because no cache-relevant fields "
              + "changed", clientId);
          return;
        }

        dynamoStreamService.extractClient(record, false)
            .ifPresentOrElse(this::upsertClient,
                () -> {
                  throw new IllegalStateException(
                      "Unable to build client from NEW_IMAGE for eventName=" + eventName);
                });
      }
      case "REMOVE" -> dynamoStreamService.extractClientId(record, true)
          .ifPresentOrElse(this::deleteClient,
              () -> {
                throw new IllegalStateException(
                    "Unable to read clientId from OLD_IMAGE for REMOVE event");
              });
      default -> Log.infof("Unsupported DynamoDB stream eventName=%s", eventName);
    }
  }

  private void upsertClient(Client client) {
    if (client.getClientId() == null || client.getClientId().isBlank()) {
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
