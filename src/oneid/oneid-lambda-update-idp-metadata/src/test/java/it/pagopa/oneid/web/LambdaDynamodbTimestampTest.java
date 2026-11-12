package it.pagopa.oneid.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.amazon.lambda.runtime.AmazonLambdaMapperRecorder;
import io.quarkus.amazon.lambda.runtime.AmazonLambdaRecorder;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.service.IDPMetadataServiceImpl;
import jakarta.inject.Inject;

/** Covers Quarkus Lambda date conversion and timestamp preservation in the generic event handler. */
@QuarkusTest
class LambdaDynamodbTimestampTest {

  @Inject
  ObjectMapper applicationMapper;

  @InjectMock
  IDPMetadataServiceImpl idpMetadataService;

  @ParameterizedTest
  @CsvSource({"1720944000, 1720944000000", "1720944000.123, 1720944000123"})
  @DisplayName("given DynamoDB epoch seconds when using the Lambda mapper then preserve exact milliseconds")
  void given_epochSeconds_when_usingLambdaMapper_then_preservesMilliseconds(
      String timestamp, long expectedMillis) throws Exception {
    // Use the mapper initialized by Quarkus for Lambda, not a manually configured copy.
    assertNotNull(AmazonLambdaMapperRecorder.objectMapper);
    DynamodbEvent event = AmazonLambdaMapperRecorder.objectMapper.readValue(
        dynamodbEvent(timestamp), DynamodbEvent.class);
    assertEquals(expectedMillis,
        event.getRecords().getFirst().getDynamodb().getApproximateCreationDateTime().getTime());
  }

  @ParameterizedTest
  @CsvSource({"1720944000, 1720944000000", "1720944000.123, 1720944000123"})
  @DisplayName("given a DynamoDB event when invoking Lambda then retain timestamp and refresh public IDPs")
  void given_dynamodbEvent_when_invokingLambda_then_retainsTimestampAndRefreshesPublicIdps(
      String timestamp, long expectedMillis) throws Exception {
    when(idpMetadataService.isPublicIdpsStatusChange(any())).thenReturn(true);
    ByteArrayInputStream input = new ByteArrayInputStream(
        dynamodbEvent(timestamp).getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    // Exercise Quarkus input reader, CDI handler selection and output writer together.
    AmazonLambdaRecorder.handle(input, output, null);

    assertEquals("Ok", applicationMapper.readValue(output.toByteArray(), String.class));
    ArgumentCaptor<JsonNode> record = ArgumentCaptor.forClass(JsonNode.class);
    verify(idpMetadataService).isPublicIdpsStatusChange(record.capture());
    verify(idpMetadataService).refreshPublicIdps();
    JsonNode actualTimestamp = record.getValue().path("dynamodb")
        .path("ApproximateCreationDateTime");
    assertEquals(expectedMillis,
        actualTimestamp.decimalValue().movePointRight(3).longValueExact());
    assertEquals("MODIFY", record.getValue().path("eventName").asText());
  }

  private static String dynamodbEvent(String timestamp) {
    return """
        {
          "Records": [{
            "eventSource": "aws:dynamodb",
            "eventName": "MODIFY",
            "dynamodb": {
              "ApproximateCreationDateTime": %s,
              "OldImage": {"status": {"S": "OK"}},
              "NewImage": {"status": {"S": "KO"}}
            }
          }]
        }
        """.formatted(timestamp);
  }
}
