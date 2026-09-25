package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;

class BrowserBindingMetricsTest {

        @Test
        @DisplayName("Anomalous ACS requests emit stable metrics for both CloudWatch alarms")
        void givenMissingBinding_whenPublishing_thenCountAndRateSeriesAgree() {
                CloudWatchConnectorImpl connector = new CloudWatchConnectorImpl();
                connector.cloudWatchAsyncClient = mock(CloudWatchAsyncClient.class);
                connector.clock = Clock.systemUTC();
                connector.CLOUDWATCH_METRIC_NAMESPACE = "io-core/ApplicationMetrics";
                when(connector.cloudWatchAsyncClient.putMetricData(any(PutMetricDataRequest.class)))
                                .thenReturn(CompletableFuture.completedFuture(PutMetricDataResponse.builder().build()));
                ArgumentCaptor<PutMetricDataRequest> requests = ArgumentCaptor.forClass(PutMetricDataRequest.class);

                connector.sendBrowserBindingMetricData("MISSING");

                verify(connector.cloudWatchAsyncClient, times(3)).putMetricData(requests.capture());
                List<String> names = requests.getAllValues().stream()
                                .map(request -> request.metricData().getFirst().metricName()).toList();
                assertTrue(names.containsAll(List.of("BrowserBindingChecked", "BrowserBindingMISSING",
                                "BrowserBindingAnomaly")));
                assertTrue(requests.getAllValues().stream()
                                .allMatch(request -> request.namespace().equals("io-core/ApplicationMetrics")));
                assertTrue(requests.getAllValues().stream()
                                .allMatch(request -> request.metricData().getFirst().dimensions().size() == 1
                                                && request.metricData().getFirst().dimensions().getFirst().name()
                                                                .equals("Cookies")
                                                && request.metricData().getFirst().dimensions().getFirst().value()
                                                                .equals("BrowserBinding")));
        }

        @Test
        @DisplayName("Legacy traffic does not dilute the anomaly rate")
        void givenLegacyRequest_whenPublishing_thenExcludeFromRateDenominator() {
                CloudWatchConnectorImpl connector = new CloudWatchConnectorImpl();
                connector.cloudWatchAsyncClient = mock(CloudWatchAsyncClient.class);
                connector.clock = Clock.systemUTC();
                connector.CLOUDWATCH_METRIC_NAMESPACE = "io-core/ApplicationMetrics";
                when(connector.cloudWatchAsyncClient.putMetricData(any(PutMetricDataRequest.class)))
                                .thenReturn(CompletableFuture.completedFuture(PutMetricDataResponse.builder().build()));
                ArgumentCaptor<PutMetricDataRequest> requests = ArgumentCaptor.forClass(PutMetricDataRequest.class);

                connector.sendBrowserBindingMetricData("LEGACY");

                verify(connector.cloudWatchAsyncClient).putMetricData(requests.capture());
                assertEquals("BrowserBindingLEGACY", requests.getValue().metricData().getFirst().metricName());
                assertEquals("Cookies", requests.getValue().metricData().getFirst().dimensions().getFirst().name());
                assertEquals("BrowserBinding",
                                requests.getValue().metricData().getFirst().dimensions().getFirst().value());
        }

        @Test
        @DisplayName("Metrics failures do not prevent subsequent browser binding metrics")
        void givenSdkFailures_whenPublishing_thenOtherMetricsAreStillAttempted() {
                CloudWatchConnectorImpl connector = new CloudWatchConnectorImpl();
                connector.cloudWatchAsyncClient = mock(CloudWatchAsyncClient.class);
                connector.clock = Clock.systemUTC();
                connector.CLOUDWATCH_METRIC_NAMESPACE = "io-core/ApplicationMetrics";
                when(connector.cloudWatchAsyncClient.putMetricData(any(PutMetricDataRequest.class)))
                                .thenThrow(new IllegalStateException("unavailable"))
                                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("unavailable")))
                                .thenReturn(CompletableFuture.completedFuture(PutMetricDataResponse.builder().build()));

                connector.sendBrowserBindingMetricData("MISSING");

                verify(connector.cloudWatchAsyncClient, times(3)).putMetricData(any(PutMetricDataRequest.class));
        }
}
