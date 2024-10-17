package it.pagopa.oneid.service;

import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.connector.ISRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@CustomLogging
public class ISServiceImpl implements ISService {

  @Inject
  @RestClient
  ISRestClient isRestClient;


  @Override
  public String getLatestIdpMetadata(String type) {
    return isRestClient.getLatestMetadata(type);
  }

}
