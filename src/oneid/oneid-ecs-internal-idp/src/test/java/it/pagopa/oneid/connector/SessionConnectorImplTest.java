package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.exception.IDPSessionException;
import it.pagopa.oneid.exception.InvalidIDPSessionUpdateException;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

@QuarkusTest
class SessionConnectorImplTest {

  private static DynamoDbTable<IDPSession> idpSessionMapper;

  @Inject
  SessionConnectorImpl sessionConnectorImpl;

  @Inject
  DynamoDbEnhancedClient dynamoDbEnhancedClient;

  @Inject
  @ConfigProperty(name = "idp_sessions_table_name")
  String TABLE_NAME;

  @BeforeEach
  public void beforeEach() {
    idpSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(IDPSession.class));

    CreateTableEnhancedRequest createTableEnhancedRequest = CreateTableEnhancedRequest.builder()
        .provisionedThroughput(
            ProvisionedThroughput.builder()
                .readCapacityUnits(10L)
                .writeCapacityUnits(10L)
                .build())
        .build();

    idpSessionMapper.createTable(createTableEnhancedRequest);
  }

  @AfterEach
  public void afterEach() {
    idpSessionMapper.deleteTable();
  }


  @Test
  void saveIDPSessionIfNotExists_success() {
    // given
    String authnRequestId = "id";
    String clientId = "client";
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .build();
    // when
    Executable executable = () -> sessionConnectorImpl.saveIDPSessionIfNotExists(idpSession);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void saveIDPSessionIfNotExists_ConditionalCheckFailedException() {
    // given
    String authnRequestId = "id";
    String clientId = "client";
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .build();
    // when
    // First insert should succeed
    assertDoesNotThrow(() -> sessionConnectorImpl.saveIDPSessionIfNotExists(idpSession));
    // Second insert with same PK/RK should throw
    assertThrows(IDPSessionException.class,
        () -> sessionConnectorImpl.saveIDPSessionIfNotExists(idpSession));
  }

  @Test
  void saveIDPSessionIfNotExists_missingPrimaryKey() {
    // given
    String authnRequestId = "";
    String clientId = "";
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .build();
    // when
    // Insert should succeed
    assertThrows(IDPSessionException.class,
        () -> sessionConnectorImpl.saveIDPSessionIfNotExists(idpSession));

  }

  @Test
  void getIDPSessionByAuthnRequestIdClientIdAndUsername_success() {
    // given
    String authnRequestId = "id";
    String clientId = "client";
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .username("user")
        .build();
    sessionConnectorImpl.saveIDPSessionIfNotExists(idpSession);

    // then
    assertEquals(Optional.of(idpSession),
        sessionConnectorImpl.getIDPSessionByAuthnRequestIdClientIdAndUsername(authnRequestId,
            clientId,
            "user"));
  }

  @Test
  void getIDPSessionByAuthnRequestIdClientIdAndUsername_notFound() {
    // given
    String authnRequestId = "id";
    String clientId = "client";
    String username = "user";

    assertEquals(Optional.empty(), sessionConnectorImpl
        .getIDPSessionByAuthnRequestIdClientIdAndUsername(authnRequestId, clientId, username));

  }

  @Test
  void findIDPSessionByAuthnRequestIdAndClientId_success() {
    // given
    String authnRequestId = "id";
    String clientId = "client";
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .username("user")
        .build();
    sessionConnectorImpl.saveIDPSessionIfNotExists(idpSession);

    // then
    assertEquals(Optional.of(idpSession),
        sessionConnectorImpl.findIDPSessionByAuthnRequestIdAndClientId(authnRequestId,
            clientId));
  }

  @Test
  void findIDPSessionByAuthnRequestIdAndClientId_notFound() {
    // given
    String authnRequestId = "id";
    String clientId = "client";

    assertEquals(Optional.empty(),
        sessionConnectorImpl.findIDPSessionByAuthnRequestIdAndClientId(authnRequestId,
            clientId));
  }

  @Test
  void updateIDPSession_success() {
    // given
    String authnRequestId = "id";
    String clientId = "client";
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .username("user")
        .build();
    sessionConnectorImpl.saveIDPSessionIfNotExists(idpSession);

    // when
    idpSession.setUsername("newUser");
    assertDoesNotThrow(() -> sessionConnectorImpl.updateIDPSession(idpSession, Optional.empty()));

    // then
    assertEquals(Optional.of(idpSession),
        sessionConnectorImpl.findIDPSessionByAuthnRequestIdAndClientId(authnRequestId,
            clientId));
  }

  @Test
  void updateIDPSession_notMatchingPreviousStatus() {
    // given
    String authnRequestId = "id";
    String clientId = "client";
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .status(
            IDPSessionStatus.TIMEOUT) // Setting a previous status that does not match the expected one
        .username("user")
        .build();

    sessionConnectorImpl.saveIDPSessionIfNotExists(idpSession);

    // when
    idpSession.setUsername("newUser");

    // Attempting to update with a different previous status needed, should throw exception
    assertThrows(InvalidIDPSessionUpdateException.class,
        () -> sessionConnectorImpl.updateIDPSession(idpSession,
            Optional.of(
                IDPSessionStatus.CREDENTIALS_VALIDATED))); // Attempting to update with a different previous status needed

  }

  @Test
  void updateIDPSession_notFound() {
    // given
    String authnRequestId = "id";
    String clientId = "client";
    IDPSession idpSession = IDPSession.builder()
        .authnRequestId(authnRequestId)
        .clientId(clientId)
        .status(
            IDPSessionStatus.TIMEOUT)
        .username("user")
        .build();

    // Attempting to update a non-existing session should throw an exception
    assertThrows(InvalidIDPSessionUpdateException.class,
        () -> sessionConnectorImpl.updateIDPSession(idpSession,
            Optional.of(
                IDPSessionStatus.TIMEOUT)));

  }
}

