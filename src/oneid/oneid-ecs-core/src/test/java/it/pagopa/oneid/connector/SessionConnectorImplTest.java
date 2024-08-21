package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@QuarkusTest
class SessionConnectorImplTest {

  private static DynamoDBProxyServer server;

  private static DynamoDbTable<SAMLSession> samlSessionMapper;
  
  @Inject
  SessionConnectorImpl<SAMLSession> samlSessionConnectorImpl;

  @Inject
  DynamoDbEnhancedClient dynamoDbEnhancedClient;

  @Inject
  @ConfigProperty(name = "sessions_table_name")
  String TABLE_NAME;

  @SneakyThrows
  @Inject
  public SessionConnectorImplTest() {
    System.setProperty("sqlite4java.library.path", "target/test/native-libs");

  }

  @BeforeAll
  public static void setupClass() throws Exception {
    String port = "8000";

    server = ServerRunner.createServerFromCommandLineArgs(
        new String[]{"-inMemory", "-port", port});
    server.start();
  }

  @AfterAll
  public static void teardownClass() throws Exception {
    server.stop();
  }

  @BeforeEach
  public void beforeEach() {
    samlSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(SAMLSession.class));
    samlSessionMapper.createTable();
  }

  @AfterEach
  public void afterEach() {
    samlSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(SAMLSession.class));
    samlSessionMapper.deleteTable();
  }

  @Test
  void saveSAMLSession() {

    //given
    SAMLSession session = new SAMLSession("id", RecordType.SAML, 0, 0, "test",
        AuthorizationRequestDTOExtended.builder().build());

    Executable executable = () -> samlSessionConnectorImpl.saveSessionIfNotExists(session);

    //then
    assertDoesNotThrow(executable);

  }

}