package it.pagopa.oneid.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.DeleteParameterResponse;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

@QuarkusTest
class SSMConnectorUtilsImplTest {

  @Inject
  SSMConnectorUtilsImpl ssmConnectorUtils;

  @InjectMock
  SsmClient ssmClient;

  @Test
  void getParameter_ok() {
    GetParameterResponse mockResponse = GetParameterResponse.builder()
        .parameter(Parameter.builder().value("secretValue").build())
        .build();

    when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(mockResponse);

    Optional<String> result = ssmConnectorUtils.getParameter("param1");

    assertTrue(result.isPresent());
    assertEquals("secretValue", result.get());
  }

  @Test
  void getParameter_ssmException_returnsEmpty() {
    AwsErrorDetails errorDetails = AwsErrorDetails.builder()
        .errorMessage("AWS error")
        .errorCode("InternalFailure")
        .build();

    SsmException ssmException = (SsmException) SsmException.builder()
        .awsErrorDetails(errorDetails)
        .build();

    when(ssmClient.getParameter(any(GetParameterRequest.class)))
        .thenThrow(ssmException);

    Optional<String> result = ssmConnectorUtils.getParameter("param1");

    assertTrue(result.isEmpty());
  }

  @Test
  void putSecureString_ok() {
    when(ssmClient.putParameter(any(PutParameterRequest.class)))
        .thenReturn(PutParameterResponse.builder().build());

    boolean result = ssmConnectorUtils.putSecureString("param1", "value");

    assertTrue(result);
  }

  @Test
  void putSecureString_ssmException_returnsFalse() {
    when(ssmClient.putParameter(any(PutParameterRequest.class)))
        .thenThrow(SsmException.builder().message("fail").build());

    boolean result = ssmConnectorUtils.putSecureString("param1", "value");

    assertFalse(result);
  }

  @Test
  void upsertSecureString_nullValue_returnsFalse() {
    assertFalse(ssmConnectorUtils.upsertSecureStringIfPresentOnlyIfChanged("param1", null));
  }

  @Test
  void upsertSecureString_emptyValue_returnsFalse() {
    assertFalse(ssmConnectorUtils.upsertSecureStringIfPresentOnlyIfChanged("param1", ""));
  }

  @Test
  void upsertSecureString_sameValue_returnsTrue_noUpdate() {
    GetParameterResponse existing = GetParameterResponse.builder()
        .parameter(Parameter.builder().value("same").build())
        .build();

    when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(existing);

    boolean result = ssmConnectorUtils.upsertSecureStringIfPresentOnlyIfChanged("param1", "same");

    assertTrue(result);
    verify(ssmClient, never()).putParameter(ArgumentMatchers.<PutParameterRequest>any());
  }

  @Test
  void upsertSecureString_valueChanged_updates() {
    GetParameterResponse existing = GetParameterResponse.builder()
        .parameter(Parameter.builder().value("old").build())
        .build();

    when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(existing);
    when(ssmClient.putParameter(any(PutParameterRequest.class)))
        .thenReturn(PutParameterResponse.builder().build());

    boolean result = ssmConnectorUtils.upsertSecureStringIfPresentOnlyIfChanged("param1", "new");

    assertTrue(result);
    verify(ssmClient).putParameter(ArgumentMatchers.<PutParameterRequest>any());
  }

  @Test
  void deleteParameter_ok() {
    when(ssmClient.deleteParameter(any(DeleteParameterRequest.class)))
        .thenReturn(DeleteParameterResponse.builder().build());

    boolean result = ssmConnectorUtils.deleteParameter("param1");

    assertTrue(result);
    verify(ssmClient).deleteParameter(any(DeleteParameterRequest.class));
  }

  @Test
  void deleteParameter_notFound_returnsTrue() {
    doThrow(ParameterNotFoundException.builder().message("missing").build())
        .when(ssmClient).deleteParameter(any(DeleteParameterRequest.class));

    boolean result = ssmConnectorUtils.deleteParameter("param1");

    assertTrue(result);
  }

  @Test
  void deleteParameter_ssmException_returnsFalse() {
    doThrow(SsmException.builder().message("fail").build())
        .when(ssmClient).deleteParameter(any(DeleteParameterRequest.class));

    boolean result = ssmConnectorUtils.deleteParameter("param1");

    assertFalse(result);
  }
}

