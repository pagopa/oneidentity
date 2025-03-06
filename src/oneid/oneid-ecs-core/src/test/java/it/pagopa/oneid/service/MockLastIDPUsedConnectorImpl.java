package it.pagopa.oneid.service;

import it.pagopa.oneid.common.connector.LastIDPUsedConnectorImpl;
import it.pagopa.oneid.common.model.LastIDPUsed;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import java.util.Optional;

@Alternative
@Dependent
public class MockLastIDPUsedConnectorImpl extends LastIDPUsedConnectorImpl {

  public MockLastIDPUsedConnectorImpl() {
    super();
  }

  @Override
  public Optional<LastIDPUsed> findLastIDPUsed(String id, String clientId) {
    return Optional.of(LastIDPUsed.builder()
        .entityId("dummy")
        .id(id)
        .clientId(clientId)
        .build());
  }

  @Override
  public void updateLastIDPUsed(LastIDPUsed lastIDPUsed) {

  }
}
