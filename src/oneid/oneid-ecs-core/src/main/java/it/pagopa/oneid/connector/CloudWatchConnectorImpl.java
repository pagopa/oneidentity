package it.pagopa.oneid.connector;

import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
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

  private final String tagIDP = "IDP";
  private final String tagSAMLStatus = "SAMLStatus";
  private final String tagClient = "Client";
  private final String tagAggregated = "Aggregated";
  private final String tagError = "Error";
  private final String tagSuccess = "Success";


  @Inject
  CloudWatchAsyncClient cloudWatchAsyncClient;
  @Inject
  Clock clock;
  @ConfigProperty(name = "cloudwatch_custom_metric_namespace")
  String CLOUDWATCH_METRIC_NAMESPACE;

  @Override
  public void sendIDPErrorMetricData(String IDP, ErrorCode errorCode) {
    List<Dimension> specificErrorDimensions = List.of(Dimension.builder()
            .name(tagIDP)
            .value(IDP)
            .build(),
        Dimension.builder()
            .name(tagError)
            .value(errorCode.name())
            .build());

    List<Dimension> totalErrorDimensions = List.of(Dimension.builder()
        .name(tagIDP + tagAggregated)
        .value(IDP)
        .build());

    // Specific error
    cloudWatchAsyncClient.putMetricData(
        generatePutMetricRequest(tagIDP + tagError, specificErrorDimensions));

    // Aggregated for IDP
    cloudWatchAsyncClient.putMetricData(generatePutMetricRequest(
        tagIDP + tagError, totalErrorDimensions));
  }

  @Override
  public void sendSAMLStatusRelatedErrorMetricData(String IDP, String client, String errorCode) {
    List<Dimension> specificUserIDPErrorDimensions = List.of(
        Dimension.builder()
            .name(tagIDP)
            .value(IDP)
            .build(),
        Dimension.builder()
            .name(tagError)
            .value(errorCode)
            .build());

    List<Dimension> specificUserClientErrorDimensions = List.of(
        Dimension.builder()
            .name(tagClient)
            .value(client)
            .build(),
        Dimension.builder()
            .name(tagError)
            .value(errorCode)
            .build());

    // Specific IDP SAMLStatus related error
    cloudWatchAsyncClient.putMetricData(
        generatePutMetricRequest(tagSAMLStatus + tagIDP + tagError,
            specificUserIDPErrorDimensions));

    // Specific Client SAMLStatus related error
    cloudWatchAsyncClient.putMetricData(
        generatePutMetricRequest(tagSAMLStatus + tagClient + tagError,
            specificUserClientErrorDimensions));


  }

  @Override
  public void sendIDPSuccessMetricData(String IDP) {

    List<Dimension> dimensions = List.of(Dimension.builder()
        .name(tagIDP + tagAggregated)
        .value(IDP)
        .build());

    cloudWatchAsyncClient.putMetricData(
        generatePutMetricRequest(tagIDP + tagSuccess, dimensions));
  }

  @Override
  public void sendClientErrorMetricData(String clientID, ErrorCode errorCode) {
    List<Dimension> specificErrorDimensions = List.of(Dimension.builder()
            .name("Client ID")
            .value(clientID)
            .build(),
        Dimension.builder()
            .name(tagError)
            .value(errorCode.name())
            .build());

    List<Dimension> totalErrorDimensions = List.of(Dimension.builder()
        .name(tagClient + tagAggregated)
        .value(clientID)
        .build());

    // Specific error
    cloudWatchAsyncClient.putMetricData(
        generatePutMetricRequest(tagClient + tagError, specificErrorDimensions));

    // Aggregated for Client
    cloudWatchAsyncClient.putMetricData(generatePutMetricRequest(
        tagClient + tagError, totalErrorDimensions));
  }

  @Override
  public void sendClientSuccessMetricData(String ClientID) {
    List<Dimension> dimensions = List.of(Dimension.builder()
        .name(tagClient + tagAggregated)
        .value(ClientID)
        .build());

    cloudWatchAsyncClient.putMetricData(
        generatePutMetricRequest(tagClient + tagSuccess, dimensions));
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
