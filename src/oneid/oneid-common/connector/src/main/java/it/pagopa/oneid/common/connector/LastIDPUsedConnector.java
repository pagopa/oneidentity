package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.LastIDPUsed;
import java.util.Optional;

public interface LastIDPUsedConnector {

  Optional<LastIDPUsed> findLastIDPUsed(String id, String clientId);

  void updateLastIDPUsed(LastIDPUsed lastIDPUsed);

}
