package it.pagopa.oneid.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonPropertyOrder({"title", "status", "detail"})
//TODO REMOVE DUPLICATES AND PUT THIS IN COMMON
public class ErrorResponse {

  private String title;

  private int status;

  private String detail;
}
