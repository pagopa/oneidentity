package it.pagopa.oneid.model;

import it.pagopa.oneid.model.enums.IDPSessionStatus;
import lombok.Data;

@Data
public class IDPSession {

  private String authnRequestId;

  private String clientId;

  private String username;

  private long timestampStart;

  private long timestampEnd;

  private IDPSessionStatus status;
}
