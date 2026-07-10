package it.pagopa.oneid.connector;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

@ApplicationScoped
public class CloudWatchConnectorImpl implements CloudWatchConnector {

  private static final String TAG_CLIENT = "Client";
  private static final String TAG_AGGREGATED = "Aggregated";
  private static final String METRIC_CLIENT_CACHE_UPDATE = "ClientCacheUpdate";

  @Inject
  CloudWatchAsyncClient cloudWatchAsyncClient;

  @Inject
  Clock clock;

  @ConfigProperty(name = "cloudwatch_custom_metric_namespace")
  String cloudWatchMetricNamespace;

  @Override
  public void sendClientCacheUpdateMetricData(String clientID) {
    List<Dimension> dimensions = List.of(Dimension.builder()
        .name(TAG_CLIENT + TAG_AGGREGATED)
        .value(clientID)
        .build());

    cloudWatchAsyncClient.putMetricData(generatePutMetricRequest(METRIC_CLIENT_CACHE_UPDATE,
        dimensions));
  }

  private PutMetricDataRequest generatePutMetricRequest(String metricName,
      List<Dimension> dimensions) {
    return PutMetricDataRequest.builder()
        .namespace(cloudWatchMetricNamespace)
        .metricData(MetricDatum.builder()
            .metricName(metricName)
            .timestamp(Instant.now(clock))
            .unit("Count")
            .value(1.0)
            .dimensions(dimensions)
            .build())
        .build();
  }
}