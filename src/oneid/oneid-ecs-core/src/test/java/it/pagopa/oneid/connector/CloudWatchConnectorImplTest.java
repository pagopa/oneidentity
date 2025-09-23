package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

class CloudWatchConnectorImplTest {

  private CloudWatchAsyncClient cloudWatchAsyncClient;
  private Clock clock;
  private CloudWatchConnectorImpl cloudWatchConnector;

  @BeforeEach
  void setUp() {
    cloudWatchAsyncClient = mock(CloudWatchAsyncClient.class);
    clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    cloudWatchConnector = new CloudWatchConnectorImpl();
    cloudWatchConnector.cloudWatchAsyncClient = cloudWatchAsyncClient;
    cloudWatchConnector.clock = clock;
    cloudWatchConnector.CLOUDWATCH_METRIC_NAMESPACE = "TestNamespace";
  }

  @Test
  void sendIDPErrorMetricData_shouldCallPutMetricDataTwice() {
    cloudWatchConnector.sendIDPErrorMetricData("testIDP", ErrorCode.AUTHORIZATION_ERROR_IDP);
    verify(cloudWatchAsyncClient, times(2)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendSAMLStatusRelatedErrorMetricData_shouldCallPutMetricDataTwice() {
    cloudWatchConnector.sendSAMLStatusRelatedErrorMetricData("idp", "client", "error");
    verify(cloudWatchAsyncClient, times(2)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendIDPSuccessMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnector.sendIDPSuccessMetricData("idp");
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendOIDynamoDBErrorMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnector.sendOIDynamoDBErrorMetricData(3);
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendOISQSErrorMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnector.sendOISQSErrorMetricData();
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendClientErrorMetricData_shouldCallPutMetricDataTwice() {
    cloudWatchConnector.sendClientErrorMetricData("clientId", ErrorCode.AUTHORIZATION_ERROR_IDP);
    verify(cloudWatchAsyncClient, times(2)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void sendClientSuccessMetricData_shouldCallPutMetricDataOnce() {
    cloudWatchConnector.sendClientSuccessMetricData("clientId");
    verify(cloudWatchAsyncClient, times(1)).putMetricData(any(PutMetricDataRequest.class));
  }

  @Test
  void putMetricDataRequest_shouldContainNamespace() {
    cloudWatchConnector.sendOISQSErrorMetricData();
    ArgumentCaptor<PutMetricDataRequest> captor = ArgumentCaptor.forClass(
        PutMetricDataRequest.class);
    verify(cloudWatchAsyncClient).putMetricData(captor.capture());
    assertEquals("TestNamespace", captor.getValue().namespace());
  }
}
