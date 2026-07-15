package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.oneid.common.connector.ClientConnector;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.Client.LocalizedContent;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.utils.dynamodb.DynamoStreamService;
import it.pagopa.oneid.common.utils.dynamodb.RecordUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class ClientPublisherServiceImplTest {

  @Test
  @DisplayName("skips republish when a modify only touches secret fields")
  void processInput_skipsRepublishWhenModifyOnlyTouchesSecretFields() throws Exception {
    ClientConnector clientConnector = Mockito.mock(ClientConnector.class);
    DynamoStreamService dynamoStreamService = Mockito.mock(DynamoStreamService.class);
    RecordUtils recordUtils = Mockito.mock(RecordUtils.class);
    S3Client s3Client = Mockito.mock(S3Client.class);
    CloudWatchClient cloudWatchClient = Mockito.mock(CloudWatchClient.class);
    ObjectMapper objectMapper = new ObjectMapper();

    Client client = Client.builder()
        .clientId("client-1")
        .friendlyName("Client One")
        .callbackURI(Set.of("https://example.com/callback"))
        .requestedParameters(Set.of("openid"))
        .authLevel(AuthLevel.L2)
        .acsIndex(1)
        .attributeIndex(2)
        .isActive(true)
        .clientIdIssuedAt(123L)
        .build();

    JsonNode record = objectMapper.readTree("""
        {
          "eventName": "MODIFY",
          "dynamodb": {
            "NewImage": {
              "clientId": {"S": "client-1"}
            }
          }
        }
        """);

    when(recordUtils.readRecords(any())).thenReturn(List.of(record));
    when(dynamoStreamService.extractClientId(record, false)).thenReturn(java.util.Optional.of("client-1"));
    when(dynamoStreamService.extractClient(record, false)).thenReturn(java.util.Optional.of(client));
    when(dynamoStreamService.extractClient(record, true)).thenReturn(java.util.Optional.of(client));

    ClientPublisherServiceImpl service = new ClientPublisherServiceImpl(
        clientConnector,
        dynamoStreamService,
        recordUtils,
        s3Client,
        cloudWatchClient,
        objectMapper,
        "clients-bucket",
        "clients/",
        "clients.json",
        "oneid-core/ApplicationMetrics");

    assertDoesNotThrow(() -> service.processInput(record));

    verify(clientConnector, never()).findAll();
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    verify(cloudWatchClient, never()).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  @DisplayName("publishes all frontend client fields")
  void processInput_publishesFrontendFields() throws Exception {
    ClientConnector clientConnector = Mockito.mock(ClientConnector.class);
    DynamoStreamService dynamoStreamService = Mockito.mock(DynamoStreamService.class);
    RecordUtils recordUtils = Mockito.mock(RecordUtils.class);
    S3Client s3Client = Mockito.mock(S3Client.class);
    CloudWatchClient cloudWatchClient = Mockito.mock(CloudWatchClient.class);
    ObjectMapper objectMapper = new ObjectMapper();
    Client client = clientWithFrontendFields("client-1");
    JsonNode record = objectMapper.readTree("""
        {
          "eventName": "INSERT",
          "dynamodb": {
            "NewImage": {
              "clientId": {"S": "client-1"}
            }
          }
        }
        """);

    when(recordUtils.readRecords(any())).thenReturn(List.of(record));
    when(dynamoStreamService.extractClientId(record, false))
        .thenReturn(java.util.Optional.of("client-1"));
    when(dynamoStreamService.extractClient(record, false))
        .thenReturn(java.util.Optional.of(client));
    when(clientConnector.findAll()).thenReturn(java.util.Optional.of(new ArrayList<>(List.of(client))));

    ClientPublisherServiceImpl service = newService(
        clientConnector, dynamoStreamService, recordUtils, s3Client, cloudWatchClient,
        objectMapper);

    service.processInput(record);

    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
    verify(s3Client, org.mockito.Mockito.times(2))
        .putObject(any(PutObjectRequest.class), bodyCaptor.capture());
    JsonNode payload = objectMapper.readTree(readBody(bodyCaptor.getAllValues().get(0)));

    assertEquals("client-1", payload.path("clientID").asText());
    assertEquals("https://example.com/logo.svg", payload.path("logoUri").asText());
    assertEquals("https://example.com/policy", payload.path("policyUri").asText());
    assertEquals("https://example.com/tos", payload.path("tosUri").asText());
    assertEquals("https://example.com/accessibility", payload.path("a11yUri").asText());
    assertEquals(true, payload.path("backButtonEnabled").asBoolean());
    assertEquals("Welcome", payload.path("localizedContentMap").path("en")
      .path("home").path("title").asText());
  }

  @Test
  @DisplayName("republishes when a frontend field changes")
  void processInput_republishesWhenFrontendFieldChanges() throws Exception {
    ClientConnector clientConnector = Mockito.mock(ClientConnector.class);
    DynamoStreamService dynamoStreamService = Mockito.mock(DynamoStreamService.class);
    RecordUtils recordUtils = Mockito.mock(RecordUtils.class);
    S3Client s3Client = Mockito.mock(S3Client.class);
    CloudWatchClient cloudWatchClient = Mockito.mock(CloudWatchClient.class);
    ObjectMapper objectMapper = new ObjectMapper();
    Client oldClient = clientWithFrontendFields("client-1", "https://example.com/logo.svg");
    Client newClient = clientWithFrontendFields("client-1", "https://example.com/new-logo.svg");
    JsonNode record = objectMapper.readTree("""
        {
          "eventName": "MODIFY",
          "dynamodb": {
            "NewImage": {
              "clientId": {"S": "client-1"}
            },
            "OldImage": {
              "clientId": {"S": "client-1"}
            }
          }
        }
        """);

    when(recordUtils.readRecords(any())).thenReturn(List.of(record));
    when(dynamoStreamService.extractClientId(record, false))
        .thenReturn(java.util.Optional.of("client-1"));
    when(dynamoStreamService.extractClient(record, false))
        .thenReturn(java.util.Optional.of(newClient));
    when(dynamoStreamService.extractClient(record, true))
        .thenReturn(java.util.Optional.of(oldClient));
    when(clientConnector.findAll())
      .thenReturn(java.util.Optional.of(new ArrayList<>(List.of(newClient))));

    ClientPublisherServiceImpl service = newService(
        clientConnector, dynamoStreamService, recordUtils, s3Client, cloudWatchClient,
        objectMapper);

    service.processInput(record);

    verify(s3Client, org.mockito.Mockito.times(2))
        .putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  private ClientPublisherServiceImpl newService(
      ClientConnector clientConnector,
      DynamoStreamService dynamoStreamService,
      RecordUtils recordUtils,
      S3Client s3Client,
      CloudWatchClient cloudWatchClient,
      ObjectMapper objectMapper) {
    return new ClientPublisherServiceImpl(
        clientConnector,
        dynamoStreamService,
        recordUtils,
        s3Client,
        cloudWatchClient,
        objectMapper,
        "clients-bucket",
        "clients/",
        "clients.json",
        "oneid-client-publisher/ApplicationMetrics");
  }

  private Client clientWithFrontendFields(String clientId) {
    return clientWithFrontendFields(clientId, "https://example.com/logo.svg");
  }

  private Client clientWithFrontendFields(String clientId, String logoUri) {
    return Client.builder()
        .clientId(clientId)
        .friendlyName("Client One")
        .callbackURI(Set.of("https://example.com/callback"))
        .requestedParameters(Set.of("openid"))
        .authLevel(AuthLevel.L2)
        .acsIndex(1)
        .attributeIndex(2)
        .isActive(true)
        .clientIdIssuedAt(123L)
        .logoUri(logoUri)
        .policyUri("https://example.com/policy")
        .tosUri("https://example.com/tos")
        .a11yUri("https://example.com/accessibility")
        .backButtonEnabled(true)
        .localizedContentMap(Map.of("en", Map.of("home", new LocalizedContent(
            "Welcome", "Description", null, null, null))))
        .build();
  }

  private String readBody(RequestBody body) throws IOException {
    try (InputStream inputStream = body.contentStreamProvider().newStream()) {
      return new String(inputStream.readAllBytes());
    }
  }
}
