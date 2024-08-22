package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.connector.model.DummySession;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.OIDCSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import jakarta.inject.Inject;
import java.time.Instant;
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
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

@QuarkusTest
class SessionConnectorImplTest {

  private static DynamoDBProxyServer server;

  private static DynamoDbTable<OIDCSession> oidcSessionMapper;

  @Inject
  SessionConnectorImpl<SAMLSession> samlSessionConnectorImpl;

  @Inject
  SessionConnectorImpl<OIDCSession> oidcSessionConnectorImpl;

  @Inject
  SessionConnectorImpl<AccessTokenSession> accessTokenSessionConnectorImpl;

  @Inject
  SessionConnectorImpl<DummySession> dummySessionConnectorImpl;

  @Inject
  DynamoDbEnhancedClient dynamoDbEnhancedClient;

  @Inject
  @ConfigProperty(name = "sessions_table_name")
  String TABLE_NAME;

  @Inject
  @ConfigProperty(name = "sessions_g_idx")
  String IDX_NAME;

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
    // Here we create the -> {sessions_table_name} table with -> {sessions_g_idx} index

    // We don't need to create a table for each mapper because it is the same for each one -> {sessions_table_name}
    // We use oidcSessionMapper (accessTokenSessionMapper would be the same) because it also contains information about GlobalSecondaryIndex -> {sessions_g_idx}
    oidcSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(OIDCSession.class));

    CreateTableEnhancedRequest createTableEnhancedRequest = CreateTableEnhancedRequest.builder()
        .provisionedThroughput(
            ProvisionedThroughput.builder()
                .readCapacityUnits(10L)
                .writeCapacityUnits(10L)
                .build())
        .globalSecondaryIndices(
            EnhancedGlobalSecondaryIndex.builder()
                .indexName(IDX_NAME)
                .projection(p -> p.projectionType(ProjectionType.ALL))
                .provisionedThroughput(ProvisionedThroughput.builder()
                    .readCapacityUnits(10L)
                    .writeCapacityUnits(10L)
                    .build())
                .build())
        .build();
    oidcSessionMapper.createTable(createTableEnhancedRequest);
  }

  @AfterEach
  public void afterEach() {
    // Here we delete the -> {sessions_table_name} table with -> {sessions_g_idx} index

    // We don't need to delete a table for each mapper because it is the same for each one -> {sessions_table_name}
    // We use the oidcSessionMapper to be sure that even GlobalSecondaryIndex is deleted -> {sessions_g_idx}
    oidcSessionMapper.deleteTable();
  }

  @Test
  void saveSAMLSession() {

    //given
    String requestId = "id";
    String samlRequest = "test";
    long creationTime = 0;
    long ttl = 0;

    SAMLSession samlSession = new SAMLSession(requestId, RecordType.SAML, creationTime, ttl,
        samlRequest,
        AuthorizationRequestDTOExtended.builder().build());

    Executable executable = () -> samlSessionConnectorImpl.saveSessionIfNotExists(samlSession);

    //then
    assertDoesNotThrow(executable);

  }

  @Test
  void saveOIDCSession() {

    //given
    String requestId = "id";
    long creationTime = 0;
    long ttl = 0;
    String authorizationCode = "code";

    OIDCSession oidcSession = new OIDCSession(requestId, RecordType.OIDC, creationTime, ttl,
        authorizationCode);

    Executable executable = () -> oidcSessionConnectorImpl.saveSessionIfNotExists(oidcSession);

    //then
    assertDoesNotThrow(executable);

  }

  @Test
  void saveAccessTokenSession() {

    //given
    String requestId = "id";
    long creationTime = 0;
    long ttl = 0;
    String accessToken = "access-token";
    String idToken = "id-token";

    AccessTokenSession accessTokenSession = new AccessTokenSession(requestId,
        RecordType.ACCESS_TOKEN, creationTime, ttl, accessToken, idToken);

    Executable executable = () -> accessTokenSessionConnectorImpl.saveSessionIfNotExists(
        accessTokenSession);

    //then
    assertDoesNotThrow(executable);

  }

  @Test
  void saveDuplicatedSession() {

    //given
    String requestId = "id";
    String samlRequest = "test";
    long creationTime = 0;
    long ttl = 0;

    SAMLSession samlSession = new SAMLSession(requestId, RecordType.SAML, creationTime, ttl,
        samlRequest,
        AuthorizationRequestDTOExtended.builder().build());

    Executable executable = () -> samlSessionConnectorImpl.saveSessionIfNotExists(samlSession);

    //then
    assertDoesNotThrow(executable);

    // re-executing the save operation with the same session should throw a SessionException
    assertThrows(SessionException.class, executable);

  }

  @Test
  void saveUnrecognizedRecordTypeSession() {

    //given
    DummySession dummySession = new DummySession();

    Executable executable = () -> dummySessionConnectorImpl.saveSessionIfNotExists(dummySession);

    //then
    assertThrows(SessionException.class, executable);

  }

  @Test
  void findSAMLSession() throws SessionException {

    //given
    String requestId = "id";
    String samlRequest = "test";
    long creationTime = Instant.now().getEpochSecond();
    long ttl = 0;

    SAMLSession samlSession = new SAMLSession(requestId, RecordType.SAML, creationTime, ttl,
        samlRequest,
        AuthorizationRequestDTOExtended.builder().build());

    Executable executable = () -> samlSessionConnectorImpl.saveSessionIfNotExists(samlSession);
    assertDoesNotThrow(executable);

    //then
    assertFalse(samlSessionConnectorImpl.findSession(requestId, RecordType.SAML).isEmpty());

  }

  @Test
  void findNotExistingSAMLSession() throws SessionException {

    //given
    String identifier = "id";
    //then
    assertTrue(samlSessionConnectorImpl.findSession(identifier, RecordType.SAML).isEmpty());

  }

  @Test
  void findExpiredSAMLSession() throws SessionException {

    //given
    String requestId = "id";
    String samlRequest = "test";
    long creationTime = 0;
    long ttl = 0;

    SAMLSession samlSession = new SAMLSession(requestId, RecordType.SAML, creationTime, ttl,
        samlRequest,
        AuthorizationRequestDTOExtended.builder().build());

    Executable executable = () -> samlSessionConnectorImpl.saveSessionIfNotExists(samlSession);
    assertDoesNotThrow(executable);

    //then
    assertTrue(samlSessionConnectorImpl.findSession(requestId, RecordType.SAML).isEmpty());

  }

  @Test
  void findOIDCSession() throws SessionException {
    //given
    String requestId = "id";
    long creationTime = Instant.now().getEpochSecond();
    long ttl = 0;
    String authorizationCode = "code";

    OIDCSession oidcSession = new OIDCSession(requestId, RecordType.OIDC, creationTime, ttl,
        authorizationCode);

    Executable executable = () -> oidcSessionConnectorImpl.saveSessionIfNotExists(oidcSession);
    assertDoesNotThrow(executable);

    //then
    assertFalse(oidcSessionConnectorImpl.findSession(authorizationCode, RecordType.OIDC).isEmpty());
  }

  @Test
  void findNotExistingOIDCSession() throws SessionException {
    //given
    String identifier = "id";
    //then
    assertTrue(oidcSessionConnectorImpl.findSession(identifier, RecordType.OIDC).isEmpty());
  }

  @Test
  void findExpiredOIDCSession() throws SessionException {
    //given
    String requestId = "id";
    long creationTime = 0;
    long ttl = 0;
    String authorizationCode = "code";

    OIDCSession oidcSession = new OIDCSession(requestId, RecordType.OIDC, creationTime, ttl,
        authorizationCode);

    Executable executable = () -> oidcSessionConnectorImpl.saveSessionIfNotExists(oidcSession);
    assertDoesNotThrow(executable);

    //then
    assertTrue(oidcSessionConnectorImpl.findSession(authorizationCode, RecordType.OIDC).isEmpty());
  }

  @Test
  void findAccessTokenSession() throws SessionException {
    //given
    String requestId = "id";
    long creationTime = Instant.now().getEpochSecond();
    long ttl = 0;
    String accessToken = "access-token";
    String idToken = "id-token";

    AccessTokenSession accessTokenSession = new AccessTokenSession(requestId,
        RecordType.ACCESS_TOKEN, creationTime, ttl, accessToken, idToken);

    Executable executable = () -> accessTokenSessionConnectorImpl.saveSessionIfNotExists(
        accessTokenSession);

    assertDoesNotThrow(executable);

    //then
    assertFalse(accessTokenSessionConnectorImpl.findSession(accessToken, RecordType.ACCESS_TOKEN)
        .isEmpty());

  }

  @Test
  void findNotExistingAccessTokenSession() throws SessionException {
    //given
    String identifier = "id";
    //then
    assertTrue(accessTokenSessionConnectorImpl.findSession(identifier, RecordType.ACCESS_TOKEN)
        .isEmpty());

  }

  @Test
  void findExpiredAccessTokenSession() throws SessionException {
    //given
    String requestId = "id";
    long creationTime = 0;
    long ttl = 0;
    String accessToken = "access-token";
    String idToken = "id-token";

    AccessTokenSession accessTokenSession = new AccessTokenSession(requestId,
        RecordType.ACCESS_TOKEN, creationTime, ttl, accessToken, idToken);

    Executable executable = () -> accessTokenSessionConnectorImpl.saveSessionIfNotExists(
        accessTokenSession);

    assertDoesNotThrow(executable);

    //then
    assertTrue(accessTokenSessionConnectorImpl.findSession(accessToken, RecordType.ACCESS_TOKEN)
        .isEmpty());

  }

  @Test
  void updateSAMLSession() {
    //given
    String requestId = "id";
    String samlRequest = "test";
    long creationTime = Instant.now().getEpochSecond();
    long ttl = 0;

    SAMLSession samlSession = new SAMLSession(requestId, RecordType.SAML, creationTime, ttl,
        samlRequest,
        AuthorizationRequestDTOExtended.builder().build());

    Executable executable = () -> samlSessionConnectorImpl.saveSessionIfNotExists(samlSession);

    assertDoesNotThrow(executable);

    //then
    String samlResponse = "test";
    executable = () -> samlSessionConnectorImpl.updateSAMLSession(requestId, samlResponse);
    assertDoesNotThrow(executable);

  }

  @Test
  void updateNotExistingValidSAMLSession() {
    //given
    String requestId = "id";
    String samlResponse = "test";

    //then
    Executable executable = () -> samlSessionConnectorImpl.updateSAMLSession(requestId,
        samlResponse);
    assertThrows(SessionException.class, executable);

  }

  @Test
  void updateSAMLSessionWithExistingSAMLResponse() {
    //given
    String requestId = "id";
    String samlRequest = "test";
    long creationTime = Instant.now().getEpochSecond();
    long ttl = 0;

    SAMLSession samlSession = new SAMLSession(requestId, RecordType.SAML, creationTime, ttl,
        samlRequest,
        AuthorizationRequestDTOExtended.builder().build());

    Executable executable = () -> samlSessionConnectorImpl.saveSessionIfNotExists(samlSession);

    assertDoesNotThrow(executable);

    String samlResponse = "test";
    executable = () -> samlSessionConnectorImpl.updateSAMLSession(requestId, samlResponse);
    assertDoesNotThrow(executable);

    //then
    assertThrows(SessionException.class, executable);

  }
}