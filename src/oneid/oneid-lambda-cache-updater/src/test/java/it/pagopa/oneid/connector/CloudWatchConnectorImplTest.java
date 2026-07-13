package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;

@QuarkusTest
class CloudWatchConnectorImplTest {

  @InjectMock
  CloudWatchClient cloudWatchClient;

  @InjectMock
  Clock clock;

  @jakarta.inject.Inject
  CloudWatchConnectorImpl cloudWatchConnector;

  @Test
  void sendClientCacheUpdateMetricData_shouldPublishDimensionedMetric() {
    when(clock.instant()).thenReturn(Instant.parse("2026-07-13T10:15:30Z"));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(cloudWatchClient.putMetricData(any(PutMetricDataRequest.class)))
        .thenReturn(PutMetricDataResponse.builder().build());

    cloudWatchConnector.sendClientCacheUpdateMetricData("client-test");

    ArgumentCaptor<PutMetricDataRequest> captor = ArgumentCaptor.forClass(PutMetricDataRequest.class);
    verify(cloudWatchClient).putMetricData(captor.capture());

    PutMetricDataRequest request = captor.getValue();
    assertEquals(1, request.metricData().size());
    assertEquals("ClientCacheUpdate", request.metricData().getFirst().metricName());
    assertEquals(1, request.metricData().getFirst().dimensions().size());
    assertEquals("ClientAggregated", request.metricData().getFirst().dimensions().getFirst().name());
    assertEquals("client-test", request.metricData().getFirst().dimensions().getFirst().value());
  }
}
