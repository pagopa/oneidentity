package it.pagopa.oneid.connector;

import it.pagopa.oneid.model.IDPInternalUser;
import java.util.Optional;

public interface InternalIDPUsersConnector {

  Optional<IDPInternalUser> getIDPInternalUserByUsernameAndNamespace(String username,
      String namespace);
}
