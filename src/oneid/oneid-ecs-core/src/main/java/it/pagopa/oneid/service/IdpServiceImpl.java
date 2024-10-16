package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class IdpServiceImpl implements IdpService {

  @Inject
  IDPConnectorImpl idpConnectorImpl;

  @ConfigProperty(name = "timestamp_spid")
  String TIMESTAMP_SPID;

  @Override
  public Optional<ArrayList<IDP>> findAllIdpByTimestamp() {
    Log.debug("start");
    return idpConnectorImpl.findIDPsByTimestamp(TIMESTAMP_SPID);
  }


}
