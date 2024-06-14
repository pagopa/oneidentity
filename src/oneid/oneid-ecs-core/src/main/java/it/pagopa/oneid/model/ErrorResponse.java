package it.pagopa.oneid.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonPropertyOrder({"type", "title", "status", "detail"})
public class ErrorResponse {

    private String type;

    private String title;

    private int status;

    private String detail;
}