package it.pagopa.oneid.common.connector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;
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
import software.amazon.awssdk.enhanced.dynamodb.Key;
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
    String userId = "test";
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
    boolean isRequiredSameIdp = false;

    ClientExtended clientExtended = new ClientExtended(clientId, userId, friendlyName, callbackURI,
        requestedParameters, authLevel, acsIndex, attributeIndex, isActive, secret, salt,
        clientIdIssuedAt, logoUri, policyUri, tosURi, isRequiredSameIdp, "", false, null, false,
        false, false
    );

    Executable executable = () -> clientConnectorImpl.saveClientIfNotExists(clientExtended);

    //then
    assertDoesNotThrow(executable);
  }

  @Test
  void getClientByAttributeConsumingServiceIndex() {
    // Insert a real Client into DynamoDB local
    ClientExtended client = ClientExtended.builder()
        .secret("test")
        .salt("test")
        .clientId("test")
        .requiredSameIdp(true)
        .userId("test1")
        .friendlyName("test1")
        .callbackURI(Set.of("test.com"))
        .requestedParameters(Set.of("test.com"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(0)
        .isActive(false)
        .clientIdIssuedAt(0)
        .logoUri("test")
        .policyUri("test")
        .tosUri("test")
        .a11yUri("test")
        .backButtonEnabled(false)
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();

    clientExtendedMapper.putItem(client);

    Optional<Client> result = clientConnectorImpl.getClientByAttributeConsumingServiceIndex(0);
    assertTrue(result.isPresent());
    assertEquals(client.getAttributeIndex(), result.get().getAttributeIndex());
  }

  @Test
  void updateClientExtended() {
    //given
    ClientExtended oldClient = ClientExtended.builder()
        .secret("originalSecret") // This field must not be overwritten
        .salt("originalSalt") // This field must not be overwritten
        .logoUri("originalLogoUri") // This field must not be overwritten
        .clientId("clientId") // same clientId to update
        .userId("oldClient") // This field will be updated
        .requiredSameIdp(true)
        .friendlyName("test1")
        .callbackURI(Set.of("test.com"))
        .requestedParameters(Set.of("test.com"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(0)
        .isActive(false)
        .clientIdIssuedAt(0)
        .build();
    clientExtendedMapper.putItem(new ClientExtended(oldClient, "test", "test"));

    ClientExtended newClientExtended = ClientExtended.builder()
        .secret("originalSecret") // This field must not be overwritten
        .salt("originalSalt") // This field must not be overwritten
        //.logoUri("") //Empty logoUri to check that it is overwritten
        .clientId("clientId") // same clientId to update
        .userId("newClient") // new userId
        .requiredSameIdp(true)
        .friendlyName("test1")
        .callbackURI(Set.of("test.com"))
        .requestedParameters(Set.of("test.com"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(0)
        .isActive(false)
        .clientIdIssuedAt(0)
        .localizedContentMap(
            Map.of("default",
                Map.of("en",
                    new Client.LocalizedContent("Title", "Description", "http://test.com",
                        null, "")
                )
            )
        )
        .build();

    //then
    Executable executable = () -> clientConnectorImpl.updateClientExtended(newClientExtended);
    assertDoesNotThrow(executable);

    ClientExtended updatedClientExtended = clientExtendedMapper.getItem(
        Key.builder().partitionValue("clientId").build());
    assertNotNull(updatedClientExtended);
    assertEquals("newClient", updatedClientExtended.getUserId()); // Updated
    assertNull(updatedClientExtended.getLogoUri()); // now is null
    assertEquals("originalSecret", newClientExtended.getSecret()); // Not overwritten
    assertEquals("originalSalt", newClientExtended.getSalt()); // Not overwritten
  }

  @Test
  void checkUniqueUserId_ok() {
    assertTrue(clientConnectorImpl.getClientByUserId("testUserId").isEmpty());
  }

  @Test
  void checkUniqueUserId_alreadyExistingUser() {
    //given
    String userId = "testUserId";
    Client client = Client.builder()
        .userId(userId)
        .logoUri("logoUri")
        .clientId("clientId")
        .requiredSameIdp(true)
        .friendlyName("test")
        .callbackURI(Set.of("test.com"))
        .requestedParameters(Set.of("test.com"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(0)
        .isActive(false)
        .clientIdIssuedAt(0)
        .build();
    clientExtendedMapper.putItem(new ClientExtended(client, "test", "test"));

    //when
    Optional<Client> result = clientConnectorImpl.getClientByUserId(userId);

    //then
    assertTrue(result.isPresent());
    assertEquals(result.get().getClientId(), client.getClientId());
    assertEquals(result.get().getUserId(), client.getUserId());
  }

  @Nested
  class TestWithClientPresent {

    @BeforeEach
    void insertClient() {
      String clientId = "test";
      String userId = "test";
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
      boolean isRequiredSameIdp = false;

      ClientExtended clientExtended = new ClientExtended(clientId, userId, friendlyName,
          callbackURI,
          requestedParameters, authLevel, acsIndex, attributeIndex, isActive, secret, salt,
          clientIdIssuedAt, logoUri, policyUri, tosURi, isRequiredSameIdp, "", false, null, false,
          false, false
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

    @Test
    void getClientExtendedById() {
      //then
      assertFalse(clientConnectorImpl.getClientExtendedById("test").isEmpty());
    }

    @Test
    void updateClientSecretSalt_updatesFields() {
      // given
      String clientId = "test";
      String newSalt = "newSaltValue";
      String newSecret = "newSecretValue";
      Client client = clientConnectorImpl.getClientById(clientId).get();

      // when
      clientConnectorImpl.updateClientSecretSalt(client, newSalt, newSecret);

      // then
      ClientExtended updated = clientConnectorImpl.getClientSecret(clientId)
          .map(secretDTO -> clientExtendedMapper.getItem(
              Key.builder().partitionValue(clientId)
                  .build()))
          .orElse(null);
      assert updated != null;
      assertEquals(newSalt, updated.getSalt());
      assertEquals(newSecret, updated.getSecret());
    }
  }
}