package it.pagopa.oneid.common.utils;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.SsmException;

@ApplicationScoped
@CustomLogging
public class SSMConnectorUtilsImpl implements SSMConnectorUtils {

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

  @Override
  public boolean putSecureString(String parameterName, String value) {
    try {
      PutParameterRequest.Builder b = PutParameterRequest.builder()
          .name(parameterName)
          .type(ParameterType.SECURE_STRING)
          .value(value)
          .overwrite(true);

      ssmClient.putParameter(b.build());
      return true;
    } catch (SsmException e) {
      Log.errorf("Error putting secure parameter '%s' to SSM: %s",
          parameterName,
          e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage());
      return false;
    }
  }

  @Override
  public boolean upsertSecureStringIfPresentOnlyIfChanged(String name, String value) {
    Optional<String> existing = getParameter(name);
    if (value == null || value.isBlank()) {
      return existing.isPresent();
    }
    String newVal = value.trim();
    if (existing.isPresent() && newVal.equals(existing.get())) {
      return true;
    }
    return putSecureString(name, newVal);
  }

  @Override
  public boolean deleteParameter(String name) {
    try {
      DeleteParameterRequest req = DeleteParameterRequest.builder()
          .name(name)
          .build();

      ssmClient.deleteParameter(req);
      return true;
    } catch (ParameterNotFoundException e) {
      Log.infof("Parameter '%s' not found in SSM, nothing to delete", name);
      return true;
    } catch (SsmException e) {
      Log.errorf("Error deleting parameter '%s' from SSM: %s",
          name,
          e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage());
      return false;
    }
  }
}
