package it.pagopa.oneid.web;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class DateDeserializationModuleCustomizerTest {

  @Test
  @DisplayName("given a fractional DynamoDB stream timestamp when deserializing then convert seconds to milliseconds")
  void given_fractionalDynamodbTimestamp_when_deserializing_then_convertsSecondsToMilliseconds()
      throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    new DateDeserializationModuleCustomizer().customize(objectMapper);

    Date timestamp = objectMapper.readValue("1720944000.123", Date.class);

    assertEquals(1720944000123L, timestamp.getTime());
  }
}
