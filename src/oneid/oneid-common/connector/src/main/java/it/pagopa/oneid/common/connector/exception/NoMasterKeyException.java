package it.pagopa.oneid.common.connector.exception;


import jakarta.ws.rs.WebApplicationException;

public class NoMasterKeyException extends WebApplicationException {

  public NoMasterKeyException(String message) {
    super(message);
  }

  public NoMasterKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}