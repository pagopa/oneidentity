package it.pagopa.oneid.connector;

import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.model.IDPSession;

public interface SessionConnector {

  void saveIDPSessionIfNotExists(IDPSession idpSession) throws OneIdentityException;

}
