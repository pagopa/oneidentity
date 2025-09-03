package it.pagopa.oneid.common.utils;

import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@ApplicationScoped
@CustomLogging
public class SSMConnectorUtilsUtilsImpl implements SSMConnectorUtils {

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
