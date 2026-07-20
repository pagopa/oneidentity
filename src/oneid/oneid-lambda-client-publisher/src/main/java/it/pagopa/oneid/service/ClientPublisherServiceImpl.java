package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnector;
import it.pagopa.oneid.common.model.ClientFE;
import it.pagopa.oneid.common.utils.dynamodb.DynamoStreamService;
import it.pagopa.oneid.common.utils.dynamodb.RecordUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ApplicationScoped
public class ClientPublisherServiceImpl implements ClientPublisherService {

  private static final String SUCCESS_METRIC_NAME = "S3PublishSuccess";
  private static final String ERROR_METRIC_NAME = "S3PublishError";
  private static final String CLIENT_ID_DIMENSION = "ClientId";
  private static final String DYNAMODB_FIELD = "dynamodb";
  private static final String ACTIVE_FIELD = "active";

  private final ClientService clientService;
  private final DynamoStreamService dynamoStreamService;
  private final RecordUtils recordUtils;
  private final S3Client s3Client;
  private final CloudWatchClient cloudWatchClient;
  private final ObjectMapper objectMapper;
  private final String bucketName;
  private final String singleClientKeyPrefix;
  private final String globalClientsKey;
  private final String namespace;

  @Inject
  ClientPublisherServiceImpl(
      ClientService clientService,
      DynamoStreamService dynamoStreamService,
      RecordUtils recordUtils,
      S3Client s3Client,
      CloudWatchClient cloudWatchClient,
      ObjectMapper objectMapper,
      @ConfigProperty(name = "clients_bucket_name") String bucketName,
      @ConfigProperty(name = "clients_key_prefix") String singleClientKeyPrefix,
      @ConfigProperty(name = "global_clients_key") String globalClientsKey,
      @ConfigProperty(name = "cloudwatch_custom_metric_namespace") String namespace) {
    this.clientService = clientService;
    this.dynamoStreamService = dynamoStreamService;
    this.recordUtils = recordUtils;
    this.s3Client = s3Client;
    this.cloudWatchClient = cloudWatchClient;
    this.objectMapper = objectMapper;
    this.bucketName = bucketName;
    this.singleClientKeyPrefix = singleClientKeyPrefix;
    this.globalClientsKey = globalClientsKey;
    this.namespace = namespace;
  }

  ClientPublisherServiceImpl(
      ClientConnector clientConnector,
      DynamoStreamService dynamoStreamService,
      RecordUtils recordUtils,
      S3Client s3Client,
      CloudWatchClient cloudWatchClient,
      ObjectMapper objectMapper,
      String bucketName,
      String singleClientKeyPrefix,
      String globalClientsKey,
      String namespace) {
    this(new ClientServiceImpl(clientConnector), dynamoStreamService, recordUtils, s3Client,
        cloudWatchClient, objectMapper, bucketName, singleClientKeyPrefix, globalClientsKey,
        namespace);
  }

  @Override
  public void runSelfBootstrap() {
    try {
        List<ClientFE> clients = clientService.getAllClientsInformation()
          .map(List::copyOf)
          .orElse(List.of());
      Log.infof("Self-bootstrap retrieved %d clients", clients.size());

      for (ClientFE client : clients) {
        publishSingleClient(client);
      }

      publishGlobalClients(clients);
      Log.infof("Self-bootstrap completed: published %d clients", clients.size());
    } catch (Exception e) {
      Log.error("Self-bootstrap failed", e);
      throw new RuntimeException("Client publisher self-bootstrap failed", e);
    }
  }

  @Override
  public void processInput(JsonNode input) {
    List<JsonNode> records = recordUtils.readRecords(input);
    if (records.isEmpty()) {
      Log.info("No DynamoDB records to process");
      return;
    }

    for (JsonNode streamRecord : records) {
      processRecord(streamRecord);
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

    String clientId = dynamoStreamService.extractClientId(streamRecord, "REMOVE".equals(eventName))
      .orElseThrow(() -> new IllegalStateException(
        "Unable to read clientId from stream image for eventName=" + eventName));

    Log.infof("Start processing clientId=%s eventName=%s", clientId, eventName);

    try {
      boolean skipPublish = false;
      switch (eventName) {
        case "INSERT", "MODIFY" -> {
          if (!isActive(streamRecord)) {
            deleteSingleClient(clientId);
            publishGlobalClients();
            publishMetric(SUCCESS_METRIC_NAME, clientId);
            break;
          }

          ClientFE client = dynamoStreamService.extractClientFE(streamRecord, false)
              .orElseThrow(() -> new IllegalStateException(
                  "Unable to build ClientFE from NEW_IMAGE for eventName=" + eventName));

          if ("MODIFY".equals(eventName)
              && !hasActiveChanged(streamRecord)
              && dynamoStreamService.extractClientFE(streamRecord, true)
              .filter(oldClient -> oldClient.equals(client))
              .isPresent()) {
            Log.infof("Skipping client republish for clientId=%s because only secret fields changed",
                client.getClientID());
            skipPublish = true;
          }

          if (!skipPublish) {
            publishSingleClient(client);
            publishGlobalClients();
            publishMetric(SUCCESS_METRIC_NAME, client.getClientID());
          }
        }
        case "REMOVE" -> {
          deleteSingleClient(clientId);
          publishGlobalClients();
          publishMetric(SUCCESS_METRIC_NAME, clientId);
        }
        default -> Log.infof("Unsupported DynamoDB stream eventName=%s", eventName);
      }
      Log.infof("End processing clientId=%s eventName=%s", clientId, eventName);
    } catch (RuntimeException e) {
      Log.errorf(e, "Error processing clientId=%s eventName=%s", clientId, eventName);
      publishMetric(ERROR_METRIC_NAME, clientId);
      throw e;
    }
  }

  private void publishSingleClient(ClientFE client) {
    String key = singleClientKeyPrefix + client.getClientID() + ".json";
    try {
      String payload = objectMapper.writeValueAsString(client);
      s3Client.putObject(PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .contentType("application/json")
              .build(),
          RequestBody.fromString(payload));
      Log.infof("Published client file s3://%s/%s", bucketName, key);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to publish client file s3://" + bucketName + "/" + key, e);
    }
  }

  private boolean isActive(JsonNode streamRecord) {
    return streamRecord.path(DYNAMODB_FIELD)
        .path("NewImage")
        .path(ACTIVE_FIELD)
        .path("BOOL")
        .asBoolean(true);
  }

  private boolean hasActiveChanged(JsonNode streamRecord) {
    JsonNode oldActive = streamRecord.path(DYNAMODB_FIELD)
        .path("OldImage")
        .path(ACTIVE_FIELD)
        .path("BOOL");
    JsonNode newActive = streamRecord.path(DYNAMODB_FIELD)
        .path("NewImage")
        .path(ACTIVE_FIELD)
        .path("BOOL");

    return !oldActive.isMissingNode()
        && !newActive.isMissingNode()
        && oldActive.asBoolean() != newActive.asBoolean();
  }

  private void deleteSingleClient(String clientId) {
    String key = singleClientKeyPrefix + clientId + ".json";
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build());
      Log.infof("Deleted client file s3://%s/%s", bucketName, key);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to delete client file s3://" + bucketName + "/" + key, e);
    }
  }

  private void publishGlobalClients() {
    List<ClientFE> clients = clientService.getAllClientsInformation()
      .map(List::copyOf)
        .orElse(List.of());
    publishGlobalClients(clients);
  }

  private void publishGlobalClients(List<ClientFE> clients) {
    try {
      String payload = objectMapper.writeValueAsString(clients);
      s3Client.putObject(PutObjectRequest.builder()
              .bucket(bucketName)
              .key(globalClientsKey)
              .contentType("application/json")
              .build(),
          RequestBody.fromString(payload));
      Log.infof("Published global clients file s3://%s/%s", bucketName, globalClientsKey);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to publish global clients file s3://" + bucketName + "/" + globalClientsKey,
          e);
    }
  }

  private void publishMetric(String metricName, String clientId) {
    try {
      Dimension dimension = Dimension.builder()
          .name(CLIENT_ID_DIMENSION)
          .value(clientId)
          .build();
      MetricDatum datum = MetricDatum.builder()
          .metricName(metricName)
          .dimensions(dimension)
          .value(1.0)
          .unit("Count")
          .build();
      PutMetricDataRequest request = PutMetricDataRequest.builder()
          .namespace(namespace)
          .metricData(datum)
          .build();
      cloudWatchClient.putMetricData(request);
      Log.debugf("Published metric %s for clientId=%s", metricName, clientId);
    } catch (Exception e) { 
      Log.warnf(e, "Failed to submit metric %s for clientId=%s", metricName, clientId);
    }
  }
}
