package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import jakarta.inject.Inject;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

@QuarkusTest
public class CloudWatchConnectorImplTest {

  @Inject
  CloudWatchConnectorImpl cloudWatchConnectorImpl;
  @InjectMock
  CloudWatchAsyncClient cloudWatchAsyncClient;
  private Clock clock;

  @Test
  void sendIDPErrorMetricData_shouldCallPutMetricDataTwice() {
    cloudWatchConnectorImpl.sendIDPErrorMetricData("testIDP", ErrorCode.AUTHORIZATION_ERROR_IDP);
    verify(cloudWatchAsyncClient, times(2)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendSAMLStatusRelatedErrorMetricData_shouldCallPutMetricDataTwice() {
    cloudWatchConnectorImpl.sendSAMLStatusRelatedErrorMetricData("idp", "client", "error");
    verify(cloudWatchAsyncClient, times(2)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendIDPSuccessMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnectorImpl.sendIDPSuccessMetricData("idp");
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendOIDynamoDBErrorMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnectorImpl.sendOIDynamoDBErrorMetricData(3);
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendOISQSErrorMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnectorImpl.sendOISQSErrorMetricData();
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendPDVErrorMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnectorImpl.sendPDVErrorMetricData(404);
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendClientErrorMetricData_shouldCallPutMetricDataTwice() {
    cloudWatchConnectorImpl.sendClientErrorMetricData("clientId",
        ErrorCode.AUTHORIZATION_ERROR_IDP);
    verify(cloudWatchAsyncClient, times(2)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendClientSuccessMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnectorImpl.sendClientSuccessMetricData("clientId");
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void putMetricDataRequest_shouldContainNamespace() {
    cloudWatchConnectorImpl.sendOISQSErrorMetricData();
    ArgumentCaptor<PutMetricDataRequest> captor = ArgumentCaptor.forClass(
        PutMetricDataRequest.class);
    verify(cloudWatchAsyncClient).putMetricData(captor.capture());
    assertEquals("ApplicationMetrics", captor.getValue().namespace());
  }

  @Test
  void sendXSWAssertionErrorMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnectorImpl.sendXSWAssertionErrorMetricData();
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendXSWAssertionErrorMetricData_shouldUseExpectedMetricName() {
    cloudWatchConnectorImpl.sendXSWAssertionErrorMetricData();

    ArgumentCaptor<PutMetricDataRequest> captor = ArgumentCaptor.forClass(
        PutMetricDataRequest.class);
    verify(cloudWatchAsyncClient).putMetricData(captor.capture());

    PutMetricDataRequest req = captor.getValue();
    assertEquals("XSWError", req.metricData().getFirst().metricName());
  }
}
