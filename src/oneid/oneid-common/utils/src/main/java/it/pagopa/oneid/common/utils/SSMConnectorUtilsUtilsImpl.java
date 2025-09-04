package it.pagopa.oneid.common.utils;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

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

    GetParameterResponse response;

    try {
      response = ssmClient.getParameter(request);
    } catch (SsmException e) {
      Log.error("Error retrieving parameter from SSM: " + e.awsErrorDetails().errorMessage());
      return Optional.empty();
    }
    return Optional.of(response.parameter().value());
  }
}
