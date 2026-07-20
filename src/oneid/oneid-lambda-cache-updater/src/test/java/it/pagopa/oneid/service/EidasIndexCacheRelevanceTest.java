package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;


@QuarkusTest
class EidasIndexCacheRelevanceTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  DynamoStreamService dynamoStreamService;

  @Test
  @DisplayName("given eidas index change when checking cache diff then return true")
  void given_eidas_index_change_when_checking_cache_diff_then_return_true() {
    ObjectNode oldImage = imageWithEidasIndex(99);
    ObjectNode newImage = imageWithEidasIndex(100);
    JsonNode streamRecord = buildRecord(oldImage, newImage);

    assertTrue(dynamoStreamService.hasCacheRelevantChanges(streamRecord));
  }

  private JsonNode buildRecord(ObjectNode oldImage, ObjectNode newImage) {
    ObjectNode record = objectMapper.createObjectNode();
    record.put("eventName", "MODIFY");
    ObjectNode dynamodb = record.putObject("dynamodb");
    dynamodb.set("OldImage", oldImage);
    dynamodb.set("NewImage", newImage);
    return record;
  }

  private ObjectNode imageWithEidasIndex(int eidasIndex) {
    ObjectNode image = objectMapper.createObjectNode();
    image.putObject("eidasIndex").put("N", eidasIndex);
    return image;
  }
}
