package it.pagopa.oneid.common.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;

public enum PairwiseMode {
  TOKEN,
  PDV;

  @JsonCreator
  public static PairwiseMode fromJson(JsonNode raw) {
    if (raw == null || raw.isNull()) {
      return null;
    }
    if (!raw.isTextual()) {
      throw new IllegalArgumentException("Pairwise mode must be a string: " + raw);
    }
    return PairwiseMode.valueOf(raw.textValue());
  }
}
