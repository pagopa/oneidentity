package it.pagopa.oneid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import io.quarkus.amazon.lambda.runtime.AmazonLambdaMapperRecorder;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class LambdaDynamodbTimestampTest {

  @ParameterizedTest
  @CsvSource({"1720944000, 1720944000000", "1720944000.123, 1720944000123"})
  @DisplayName("given DynamoDB epoch seconds when using the Lambda mapper then preserve exact milliseconds")
  void given_epochSeconds_when_usingLambdaMapper_then_preservesMilliseconds(
      String timestamp, long expectedMillis) throws Exception {
    assertNotNull(AmazonLambdaMapperRecorder.objectMapper);
    DynamodbEvent event = AmazonLambdaMapperRecorder.objectMapper.readValue("""
        {
          "Records": [{
            "eventSource": "aws:dynamodb",
            "dynamodb": {"ApproximateCreationDateTime": %s}
          }]
        }
        """.formatted(timestamp), DynamodbEvent.class);

    assertEquals(expectedMillis,
        event.getRecords().getFirst().getDynamodb().getApproximateCreationDateTime().getTime());
  }
}
