package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.connector.ISRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ISServiceImpl implements ISService {

  @Inject
  @RestClient
  ISRestClient isRestClient;


  @Override
  public String getLatestIdpMetadata(String type) {
    Log.debug("start");
    return isRestClient.getLatestMetadata(type);
  }

}
