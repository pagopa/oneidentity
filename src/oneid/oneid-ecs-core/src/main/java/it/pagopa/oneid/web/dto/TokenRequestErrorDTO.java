package it.pagopa.oneid.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonPropertyOrder({"error", "error_description"})
public class TokenRequestErrorDTO {

  private String error;

  @JsonProperty("error_description")
  private String errorDescription;
}