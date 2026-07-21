package it.pagopa.oneid.common.utils.dynamodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class RecordUtilsImplTest {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  RecordUtils recordUtils;

  @Test
  @DisplayName("given null root when reading records then return empty list")
  void given_null_root_when_reading_records_then_return_empty_list() {
    assertTrue(recordUtils.readRecords(null).isEmpty());
  }

  @Test
  @DisplayName("given array root when reading records then return array entries")
  void given_array_root_when_reading_records_then_return_array_entries() {
    List<JsonNode> records = recordUtils.readRecords(objectMapper.createArrayNode().add(buildRecord()));

    assertEquals(1, records.size());
    assertEquals("INSERT", records.get(0).path("eventName").asText());
  }

  @Test
  @DisplayName("given Records wrapper when reading records then return wrapper entries")
  void given_records_wrapper_when_reading_records_then_return_wrapper_entries() {
    ObjectNode root = objectMapper.createObjectNode();
    root.putArray("Records").add(buildRecord());

    List<JsonNode> records = recordUtils.readRecords(root);

    assertEquals(1, records.size());
    assertEquals("INSERT", records.get(0).path("eventName").asText());
  }

  @Test
  @DisplayName("given records wrapper when reading records then return lowercase wrapper entries")
  void given_records_wrapper_when_reading_records_then_return_lowercase_wrapper_entries() {
    ObjectNode root = objectMapper.createObjectNode();
    root.putArray("records").add(buildRecord());

    List<JsonNode> records = recordUtils.readRecords(root);

    assertEquals(1, records.size());
    assertEquals("INSERT", records.get(0).path("eventName").asText());
  }

  @Test
  @DisplayName("given single object when reading records then return singleton list")
  void given_single_object_when_reading_records_then_return_singleton_list() {
    List<JsonNode> records = recordUtils.readRecords(buildRecord());

    assertEquals(1, records.size());
    assertEquals("INSERT", records.get(0).path("eventName").asText());
  }

  @Test
  @DisplayName("given complete modify record when checking completeness then return false")
  void given_complete_modify_record_when_checking_completeness_then_return_false() {
    assertFalse(recordUtils.isIncompleteModifyRecord(buildModifyRecord(true, true)));
  }

  @Test
  @DisplayName("given modify record without dynamodb when checking completeness then return true")
  void given_modify_record_without_dynamodb_when_checking_completeness_then_return_true() {
    assertTrue(recordUtils.isIncompleteModifyRecord(objectMapper.createObjectNode()));
  }

  @Test
  @DisplayName("given modify record without old image when checking completeness then return true")
  void given_modify_record_without_old_image_when_checking_completeness_then_return_true() {
    assertTrue(recordUtils.isIncompleteModifyRecord(buildModifyRecord(false, true)));
  }

  @Test
  @DisplayName("given modify record without new image when checking completeness then return true")
  void given_modify_record_without_new_image_when_checking_completeness_then_return_true() {
    assertTrue(recordUtils.isIncompleteModifyRecord(buildModifyRecord(true, false)));
  }

  private ObjectNode buildRecord() {
    return objectMapper.createObjectNode().put("eventName", "INSERT");
  }

  private ObjectNode buildModifyRecord(boolean includeOldImage, boolean includeNewImage) {
    ObjectNode streamRecord = objectMapper.createObjectNode();
    ObjectNode dynamodb = streamRecord.putObject("dynamodb");
    if (includeOldImage) {
      dynamodb.set("OldImage", objectMapper.createObjectNode().put("clientId", "client-test"));
    }
    if (includeNewImage) {
      dynamodb.set("NewImage", objectMapper.createObjectNode().put("clientId", "client-test"));
    }
    return streamRecord;
  }
}
