package it.pagopa.oneid.common.connector;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@ApplicationScoped
public class SSMConnectorImpl implements SSMConnector {

  @Inject
  SsmClient ssmClient;

  @Override
  public Optional<String> getParameter(String parameterName) {
    GetParameterRequest request = GetParameterRequest.builder()
        .name(parameterName)
        .withDecryption(true)  // Set to true if the parameter is encrypted
        .build();

    // TODO: manage exceptions from AWS SDK
    GetParameterResponse response = ssmClient.getParameter(request);
    return Optional.of(response.parameter().value());
  }
}
