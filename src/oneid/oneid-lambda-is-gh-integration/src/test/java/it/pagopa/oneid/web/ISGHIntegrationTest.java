package it.pagopa.oneid.web;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.service.GitHubServiceImpl;
import it.pagopa.oneid.service.ISServiceImpl;
import jakarta.inject.Inject;

@QuarkusTest
class ISGHIntegrationTest {

  @Inject
  ISGHIntegration isGhIntegration;

  @InjectMock
  ISServiceImpl isServiceImpl;
  @InjectMock
  GitHubServiceImpl gitHubServiceImpl;

  private Context context;
  private SNSEvent event;
  private SNSRecord record;
  private SNS sns;

  // TODO uncomment and fix tests
  /*

  @BeforeEach
  public void beforeEach() {
    context = mock(Context.class);
    event = mock(SNSEvent.class);
    record = mock(SNSRecord.class);
    sns = mock(SNS.class);
  }

  @Test
  void handleRequest_OK() {

    String snsMessage = "{\n"
        + "  \"data\": {\n"
        + "    \"TAG\": \"1111\",\n"
        + "    \"OBJ\": \"spid\"\n"
        + "  }\n"
        + "}";

    when(sns.getMessage()).thenReturn(snsMessage);
    when(record.getSNS()).thenReturn(sns);

    List<SNSRecord> records = new ArrayList<>(List.of(record));
    when(event.getRecords()).thenReturn(records);

    String response = isGhIntegration.handleRequest(event, context);

    Executable executable = () -> isGhIntegration.handleRequest(event, context);
    Assertions.assertDoesNotThrow(executable);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(snsMessage, response);
  }

  @ParameterizedTest
  @ValueSource(strings = {"{invalid json}", ""})
  void handleRequest_emptyOrInvalidMessageException(String snsMessage) {

    when(sns.getMessage()).thenReturn(snsMessage);
    when(record.getSNS()).thenReturn(sns);

    List<SNSRecord> records = new ArrayList<>(List.of(record));
    when(event.getRecords()).thenReturn(records);

    Executable executable = () -> isGhIntegration.handleRequest(event, context);
    Assertions.assertThrows(RuntimeException.class, executable);

  }

*/
}