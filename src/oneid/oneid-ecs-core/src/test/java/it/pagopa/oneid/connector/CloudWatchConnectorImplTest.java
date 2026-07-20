package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;

@QuarkusTest
public class CloudWatchConnectorImplTest {

  @Inject
  CloudWatchConnectorImpl cloudWatchConnectorImpl;
  @InjectMock
  CloudWatchAsyncClient cloudWatchAsyncClient;

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
  void sendClientCacheMissMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnectorImpl.sendClientCacheMissMetricData("clientId");
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendClientCacheBackfillSuccessMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnectorImpl.sendClientCacheBackfillSuccessMetricData("clientId");
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendClientCacheBackfillFailureMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnectorImpl.sendClientCacheBackfillFailureMetricData("clientId");
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendClientCacheMissMetricData_shouldUseExpectedMetricName() {
    cloudWatchConnectorImpl.sendClientCacheMissMetricData("clientId");

    ArgumentCaptor<PutMetricDataRequest> captor = ArgumentCaptor.forClass(PutMetricDataRequest.class);
    verify(cloudWatchAsyncClient).putMetricData(captor.capture());

    PutMetricDataRequest req = captor.getValue();
    assertEquals("ClientCacheMiss", req.metricData().getFirst().metricName());
  }

  @Test
  void sendClientCacheBackfillSuccessMetricData_shouldUseExpectedMetricName() {
    cloudWatchConnectorImpl.sendClientCacheBackfillSuccessMetricData("clientId");

    ArgumentCaptor<PutMetricDataRequest> captor = ArgumentCaptor.forClass(PutMetricDataRequest.class);
    verify(cloudWatchAsyncClient).putMetricData(captor.capture());

    PutMetricDataRequest req = captor.getValue();
    assertEquals("ClientCacheBackfillSuccess", req.metricData().getFirst().metricName());
  }

  @Test
  void sendClientCacheBackfillFailureMetricData_shouldUseExpectedMetricName() {
    cloudWatchConnectorImpl.sendClientCacheBackfillFailureMetricData("clientId");

    ArgumentCaptor<PutMetricDataRequest> captor = ArgumentCaptor.forClass(PutMetricDataRequest.class);
    verify(cloudWatchAsyncClient).putMetricData(captor.capture());

    PutMetricDataRequest req = captor.getValue();
    assertEquals("ClientCacheBackfillFailure", req.metricData().getFirst().metricName());
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
    when(cloudWatchAsyncClient.putMetricData(any(PutMetricDataRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(PutMetricDataResponse.builder().build()));
    cloudWatchConnectorImpl.sendXSWAssertionErrorMetricData();
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendXSWAssertionErrorMetricData_shouldUseExpectedMetricName() {
    when(cloudWatchAsyncClient.putMetricData(any(PutMetricDataRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(PutMetricDataResponse.builder().build()));
    cloudWatchConnectorImpl.sendXSWAssertionErrorMetricData();

    ArgumentCaptor<PutMetricDataRequest> captor = ArgumentCaptor.forClass(
        PutMetricDataRequest.class);
    verify(cloudWatchAsyncClient).putMetricData(captor.capture());

    PutMetricDataRequest req = captor.getValue();
    assertEquals("XSWError", req.metricData().getFirst().metricName());
  }
}
