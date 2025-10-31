package it.pagopa.oneid.common.connector.exception;

import jakarta.ws.rs.WebApplicationException;
import java.util.Optional;

public class PDVException extends WebApplicationException {

  private final Integer status;
  private final Optional<String> payload;

  public PDVException(String message, Integer status, Optional<String> payload, Throwable cause) {
    super(message, cause);
    this.status = status;
    this.payload = payload;
  }

  public Integer getStatus() {
    return status;
  }

  public Optional<String> getPayload() {
    return payload;
  }
}
