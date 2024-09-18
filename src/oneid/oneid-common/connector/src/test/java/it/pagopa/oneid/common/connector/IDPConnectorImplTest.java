package it.pagopa.oneid.common.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.IDPStatus;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

@QuarkusTest
class IDPConnectorImplTest {

  private static DynamoDBProxyServer server;

  private static DynamoDbTable<IDP> idpMapper;

  @Inject
  IDPConnectorImpl idpConnectorImpl;

  @Inject
  DynamoDbEnhancedClient dynamoDbEnhancedClient;

  @Inject
  @ConfigProperty(name = "idp_table_name")
  String TABLE_NAME;

  @Inject
  @ConfigProperty(name = "idp_g_idx")
  String IDX_NAME;

  @Inject
  public IDPConnectorImplTest() {
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
    // Here we create the -> {idp_table_name} table with -> {idp_g_idx} index

    idpMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(IDP.class));

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
    idpMapper.createTable(createTableEnhancedRequest);
  }

  @AfterEach
  public void afterEach() {
    // Here we delete the -> {idp_table_name} table with -> {idp_g_idx} index
    idpMapper.deleteTable();
  }

  //endregion

  @Test
  void findIDPsByTimestamp() {
  }

  @Test
  void getIDPByEntityIDAndTimestamp_notEmpty() {

    //given
    String entityId = "test";
    String pointer = LatestTAG.LATEST_SPID.toString();
    long timestamp = 12345;
    boolean isActive = true;
    IDPStatus idpStatus = IDPStatus.OK;
    Map<String, String> idpSSOEndpoint = Collections.singletonMap("test", "test");
    Set<String> certificates = Collections.singleton("test");
    String friendlyName = "test";

    IDP idp = new IDP(entityId, pointer, timestamp, isActive, idpStatus, idpSSOEndpoint,
        certificates, friendlyName);

    ArrayList<IDP> idps = new ArrayList<>(Collections.singleton(idp));

    //then
    idpConnectorImpl.saveIDPs(idps, LatestTAG.LATEST_SPID);

    Optional<ArrayList<IDP>> idpList = idpConnectorImpl.findIDPsByTimestamp(
        String.valueOf(LatestTAG.LATEST_SPID));

    assertTrue(idpList.isPresent());
    assertEquals(1, idpList.get().size());
  }

  @Test
  void getIDPByEntityIDAndTimestamp_Empty() {
    
    Optional<ArrayList<IDP>> idpList = idpConnectorImpl.findIDPsByTimestamp(
        String.valueOf(LatestTAG.LATEST_SPID));

    assertFalse(idpList.isPresent());
  }

  @Test
  void saveIDPs_emptyDB() {

    //given
    String entityId = "test";
    String pointer = LatestTAG.LATEST_SPID.toString();
    long timestamp = 12345;
    boolean isActive = true;
    IDPStatus idpStatus = IDPStatus.OK;
    Map<String, String> idpSSOEndpoint = Collections.singletonMap("test", "test");
    Set<String> certificates = Collections.singleton("test");
    String friendlyName = "test";

    IDP idp = new IDP(entityId, pointer, timestamp, isActive, idpStatus, idpSSOEndpoint,
        certificates, friendlyName);

    ArrayList<IDP> idps = new ArrayList<>(Collections.singleton(idp));

    //then
    idpConnectorImpl.saveIDPs(idps, LatestTAG.LATEST_SPID);

    IDP savedIdp = idpConnectorImpl.getIDPByEntityIDAndTimestamp(entityId,
        String.valueOf(LatestTAG.LATEST_SPID)).get();

    assertEquals(savedIdp.getPointer(), String.valueOf(LatestTAG.LATEST_SPID));

  }

  @Test
  void saveIDPs_alreadyExistingEntry() {

    //given
    String entityId = "test";
    String pointer = LatestTAG.LATEST_SPID.toString();
    long timestamp = 12345;
    boolean isActive = true;
    IDPStatus idpStatus = IDPStatus.OK;
    Map<String, String> idpSSOEndpoint = Collections.singletonMap("test", "test");
    Set<String> certificates = Collections.singleton("test");
    String friendlyName = "test";

    IDP idp = new IDP(entityId, pointer, timestamp, isActive, idpStatus, idpSSOEndpoint,
        certificates, friendlyName);

    ArrayList<IDP> idps = new ArrayList<>(Collections.singleton(idp));

    idpConnectorImpl.saveIDPs(idps, LatestTAG.LATEST_SPID);

    IDP savedIdp = idpConnectorImpl.getIDPByEntityIDAndTimestamp(entityId,
        String.valueOf(LatestTAG.LATEST_SPID)).get();

    assertEquals(savedIdp.getPointer(), String.valueOf(LatestTAG.LATEST_SPID));
    assertFalse(idpConnectorImpl.getIDPByEntityIDAndTimestamp(entityId, String.valueOf(timestamp))
        .isPresent());

    // then

    IDP updatedIdp = new IDP(entityId, pointer, timestamp + 10, isActive, idpStatus, idpSSOEndpoint,
        certificates, friendlyName);

    ArrayList<IDP> updatedIdps = new ArrayList<>(Collections.singleton(updatedIdp));

    idpConnectorImpl.saveIDPs(updatedIdps, LatestTAG.LATEST_SPID);

    IDP newIdp = idpConnectorImpl.getIDPByEntityIDAndTimestamp(entityId,
        String.valueOf(LatestTAG.LATEST_SPID)).get();

    assertEquals(newIdp.getPointer(), String.valueOf(LatestTAG.LATEST_SPID));
    assertTrue(idpConnectorImpl.getIDPByEntityIDAndTimestamp(entityId, String.valueOf(timestamp))
        .isPresent());


  }
}