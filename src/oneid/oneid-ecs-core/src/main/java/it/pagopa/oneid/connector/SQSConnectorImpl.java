package it.pagopa.oneid.connector;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SqsException;

@ApplicationScoped
public class SQSConnectorImpl implements SQSConnector {

  @Inject
  SqsClient sqsClient;

  @Inject
  CloudWatchConnectorImpl cloudWatchConnectorImpl;

  @ConfigProperty(name = "pdv_error_queue_url")
  String pdvErrorQueueUrl;

  @Override
  public void sendMessage(String messageBody) {
    try {
      sqsClient.sendMessage(builder -> builder.queueUrl(pdvErrorQueueUrl).messageBody(messageBody));
    } catch (SqsException e) {
      // Log in case of failure to send message to SQS but do not throw exception to avoid blocking the main process
      Log.error("Error sending message to SQS queue: " + e.getMessage());
      // Send metric to CloudWatch for monitoring purposes
      cloudWatchConnectorImpl.sendOISQSErrorMetricData();
    }
  }
}
