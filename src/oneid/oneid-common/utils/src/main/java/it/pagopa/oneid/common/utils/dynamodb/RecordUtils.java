package it.pagopa.oneid.common.utils.dynamodb;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface RecordUtils {

  List<JsonNode> readRecords(JsonNode root);

  boolean isIncompleteModifyRecord(JsonNode streamRecord);
}
