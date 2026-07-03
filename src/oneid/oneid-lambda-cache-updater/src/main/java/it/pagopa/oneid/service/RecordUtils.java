package it.pagopa.oneid.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface RecordUtils {
  List<JsonNode> readRecords(JsonNode root);
  boolean isIncompleteModifyRecord(JsonNode record);
}
