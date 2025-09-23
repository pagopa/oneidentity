package it.pagopa.oneid.connector;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SqsException;

@QuarkusTest
public class SQSConnectorImplTest {

  @Inject
  SQSConnectorImpl sqsConnectorImpl;

  @InjectMock
  SqsClient sqsClientMock;

  @InjectMock
  CloudWatchConnectorImpl cloudWatchConnectorMock;


  @Test
  void sendMessage_ok() {
    sqsConnectorImpl.sendMessage("test message");
    Mockito.verify(sqsClientMock, Mockito.times(1))
        .sendMessage(Mockito.any(Consumer.class));
    Mockito.verifyNoInteractions(cloudWatchConnectorMock);
  }

  @Test
  void sendMessage_ko() {
    Mockito.doThrow(SqsException.class)
        .when(sqsClientMock)
        .sendMessage(Mockito.any(Consumer.class));
    sqsConnectorImpl.sendMessage("test message");
    Mockito.verify(sqsClientMock, Mockito.times(1))
        .sendMessage(Mockito.any(Consumer.class));
    Mockito.verify(cloudWatchConnectorMock, Mockito.times(1))
        .sendOISQSErrorMetricData();
  }

}
