package it.pagopa.oneid.connector;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

@ApplicationScoped
public class CloudWatchConnectorImpl implements CloudWatchConnector {

  private static final String TAG_CLIENT_AGGREGATED = "ClientAggregated";
  private static final String METRIC_CLIENT_CACHE_UPDATE = "ClientCacheUpdate";
  private static final String METRIC_CLIENT_CACHE_UPDATE_FAILURE = "ClientCacheUpdateFailure";

  private final CloudWatchClient cloudWatchClient;

  private final Clock clock;

  public CloudWatchConnectorImpl(CloudWatchClient cloudWatchClient, Clock clock) {
    this.cloudWatchClient = cloudWatchClient;
    this.clock = clock;
  }

  @ConfigProperty(name = "cloudwatch_custom_metric_namespace")
  String cloudWatchMetricNamespace;

  @Override
  public void sendClientCacheUpdateMetricData(String clientID) {
    MetricDatum metricDatum = buildMetricDatum(METRIC_CLIENT_CACHE_UPDATE, clientID);
    try {
      cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
          .namespace(cloudWatchMetricNamespace)
          .metricData(metricDatum)
          .build());
    } catch (Exception e) {
      Log.errorf(e, "Failed to publish ClientCacheUpdate metric for clientId=%s", clientID);
    }
  }

  @Override
  public void sendClientCacheUpdateFailureMetricData(String clientID) {
    MetricDatum metricDatum = buildMetricDatum(METRIC_CLIENT_CACHE_UPDATE_FAILURE, clientID);
    try {
      cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
          .namespace(cloudWatchMetricNamespace)
          .metricData(metricDatum)
          .build());
    } catch (Exception e) {
      Log.errorf(e, "Failed to publish ClientCacheUpdateFailure metric for clientId=%s",
          clientID);
    }
  }

  private MetricDatum buildMetricDatum(String metricName, String clientID) {
    Instant eventTime = Instant.now(clock);
    List<Dimension> dimensions = List.of(Dimension.builder()
        .name(TAG_CLIENT_AGGREGATED)
        .value(clientID)
        .build());

    return MetricDatum.builder()
        .metricName(metricName)
        .timestamp(eventTime)
        .unit("Count")
        .value(1.0)
        .dimensions(dimensions)
        .build();
  }
}
