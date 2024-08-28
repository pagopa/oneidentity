package it.pagopa.oneid.common.connector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import jakarta.inject.Inject;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.CollectionUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@QuarkusTest
class ClientConnectorImplTest {

  private static DynamoDBProxyServer server;

  private static DynamoDbTable<ClientExtended> clientExtendedMapper;

  @Inject
  ClientConnectorImpl clientConnectorImpl;

  @Inject
  DynamoDbEnhancedClient dynamoDbEnhancedClient;

  @Inject
  @ConfigProperty(name = "client_registrations_table_name")
  String TABLE_NAME;

  @Inject
  public ClientConnectorImplTest() {
    System.setProperty("sqlite4java.library.path", "target/test/native-libs");
  }

  //region Before & After
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
    clientExtendedMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(ClientExtended.class));
    clientExtendedMapper.createTable();
  }


  @AfterEach
  public void afterEach() {
    clientExtendedMapper.deleteTable();
  }

  //endregion


  @Test
  void findAllEmpty() {
    //then
    assertTrue(clientConnectorImpl.findAll().isEmpty());
  }

  @Test
  void saveClientIfNotExists() {

    //given
    String clientId = "test";
    String friendlyName = "test";
    Set<String> callbackURI = CollectionUtils.toSet(new String[]{"test"});
    Set<String> requestedParameters = CollectionUtils.toSet(new String[]{"test"});
    AuthLevel authLevel = AuthLevel.L2;
    int acsIndex = 0;
    int attributeIndex = 0;
    boolean isActive = true;
    String secret = "test";
    String salt = "test";
    long clientIdIssuedAt = 0L;
    String logoUri = "test";
    String policyUri = "test";
    String tosURi = "test";

    ClientExtended clientExtended = new ClientExtended(clientId, friendlyName, callbackURI,
        requestedParameters, authLevel, acsIndex, attributeIndex, isActive, secret, salt,
        clientIdIssuedAt, logoUri, policyUri, tosURi
    );

    Executable executable = () -> clientConnectorImpl.saveClientIfNotExists(clientExtended);

    //then
    assertDoesNotThrow(executable);
  }

  @Nested
  class TestWithClientPresent {

    @BeforeEach
    void insertClient() {
      String clientId = "test";
      String friendlyName = "test";
      Set<String> callbackURI = CollectionUtils.toSet(new String[]{"test"});
      Set<String> requestedParameters = CollectionUtils.toSet(new String[]{"test"});
      AuthLevel authLevel = AuthLevel.L2;
      int acsIndex = 0;
      int attributeIndex = 0;
      boolean isActive = true;
      String secret = "test";
      String salt = "test";
      long clientIdIssuedAt = 0L;
      String logoUri = "test";
      String policyUri = "test";
      String tosURi = "test";

      ClientExtended clientExtended = new ClientExtended(clientId, friendlyName, callbackURI,
          requestedParameters, authLevel, acsIndex, attributeIndex, isActive, secret, salt,
          clientIdIssuedAt, logoUri, policyUri, tosURi
      );

      clientConnectorImpl.saveClientIfNotExists(clientExtended);
    }

    @Test
    void findAllNotEmpty() {
      //then
      assertFalse(clientConnectorImpl.findAll().isEmpty());
    }

    @Test
    void getClientSecret() {
      //then
      assertFalse(clientConnectorImpl.getClientSecret("test").isEmpty());
    }

    @Test
    void getClientById() {
      //then
      assertFalse(clientConnectorImpl.getClientById("test").isEmpty());
    }
  }
}