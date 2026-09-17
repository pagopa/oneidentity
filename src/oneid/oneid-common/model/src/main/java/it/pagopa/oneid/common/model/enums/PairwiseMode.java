package it.pagopa.oneid.common.model.enums;

public enum PairwiseMode {
  TOKEN,
  PDV;

  public static PairwiseMode fromLegacy(Object raw) {
    if (raw == null) {
      return null;
    }
    if (raw instanceof PairwiseMode mode) {
      return mode;
    }
    if (raw instanceof Boolean enabled) {
      return enabled ? TOKEN : null;
    }
    if (raw instanceof String value) {
      String normalized = value.trim();
      if (normalized.isEmpty() || "false".equalsIgnoreCase(normalized)) {
        return null;
      }
      if ("true".equalsIgnoreCase(normalized)) {
        return TOKEN;
      }
      try {
        return PairwiseMode.valueOf(normalized);
      } catch (IllegalArgumentException exception) {
        throw new IllegalArgumentException("Unsupported pairwise value: " + raw, exception);
      }
    }
    throw new IllegalArgumentException("Unsupported pairwise value: " + raw);
  }
}
