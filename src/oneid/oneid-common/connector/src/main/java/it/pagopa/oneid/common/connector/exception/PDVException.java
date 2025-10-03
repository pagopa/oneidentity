package it.pagopa.oneid.common.connector.exception;

import jakarta.ws.rs.WebApplicationException;

public class PDVException extends WebApplicationException {

  private final Integer status;
  private final String payload;

  public PDVException(String message, Integer status, String payload, Throwable cause) {
    super(message, cause);
    this.status = status;
    this.payload = payload;
  }

  public PDVException(String message, WebApplicationException cause) {
    super(message, cause,
        cause.getResponse() != null ? cause.getResponse() : null);
    Integer s = null;
    String body = null;
    if (cause.getResponse() != null) {
      s = cause.getResponse().getStatus();
      try {
        body = cause.getResponse().readEntity(String.class);
      } catch (Exception ignored) {
      }
    }
    this.status = s;
    this.payload = body;
  }

  public Integer getStatus() {
    return status;
  }

  public String getPayload() {
    return payload;
  }
}