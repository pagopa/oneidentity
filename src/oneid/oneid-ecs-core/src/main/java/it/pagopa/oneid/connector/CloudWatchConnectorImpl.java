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

  @Inject
  CloudWatchAsyncClient cloudWatchAsyncClient;

  @Inject
  Clock clock;

  @ConfigProperty(name = "cloudwatch_custom_metric_namespace")
  String CLOUDWATCH_METRIC_NAMESPACE;

  @Override
  public void sendIDPErrorMetricData(String IDP, String errorMessage) {
    // TODO: implement
  }

  @Override
  public void sendIDPSuccessMetricData(String IDP) {

    List<Dimension> dimensions = List.of(Dimension.builder()
        .name("IDP")
        .value(IDP)
        .build());

    cloudWatchAsyncClient.putMetricData(
        generatePutMetricRequest("IDPSuccess", dimensions));
  }

  @Override
  public void sendClientErrorMetricData(String clientID, String errorMessage) {
    // TODO: implement
  }

  @Override
  public void sendClientSuccessMetricData(String ClientID) {
    List<Dimension> dimensions = List.of(Dimension.builder()
        .name("Client ID")
        .value(ClientID)
        .build());

    cloudWatchAsyncClient.putMetricData(
        generatePutMetricRequest("ClientSuccess", dimensions));
  }

  private PutMetricDataRequest generatePutMetricRequest(String metricName,
      List<Dimension> dimensions) {
    return PutMetricDataRequest.builder()
        .namespace(CLOUDWATCH_METRIC_NAMESPACE)
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
