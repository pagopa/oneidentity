package it.pagopa.oneid.service;

import io.quarkus.runtime.Startup;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@CustomLogging
@Startup
public class IdpServiceImpl implements IdpService {

  @Inject
  IDPConnectorImpl idpConnectorImpl;

  @ConfigProperty(name = "timestamp_spid")
  String TIMESTAMP_SPID;

  @Override
  public Optional<ArrayList<IDP>> findAllIdpByTimestamp() {
    return idpConnectorImpl.findIDPsByTimestamp(TIMESTAMP_SPID);
  }


}
