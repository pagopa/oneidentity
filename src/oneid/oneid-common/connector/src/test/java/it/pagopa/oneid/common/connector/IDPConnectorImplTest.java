package it.pagopa.oneid.common.connector;

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
  void getIDPByEntityIDAndTimestamp() {
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

    //todo maybe add some check on db?

  }
}