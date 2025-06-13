package it.pagopa.oneid.connector;

import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.model.IDPInternalUser;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

@QuarkusTest
public class InternalIDPUsersConnectorImplTest {

  private static DynamoDbTable<IDPInternalUser> idpInternalUserMapper;

  @Inject
  InternalIDPUsersConnectorImpl internalIDPUsersConnector;

  @Inject
  DynamoDbEnhancedClient dynamoDbEnhancedClient;

  @Inject
  @ConfigProperty(name = "idp_internal_users_table_name")
  String TABLE_NAME;

  @BeforeEach
  public void beforeEach() {
    idpInternalUserMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(IDPInternalUser.class));

    CreateTableEnhancedRequest createTableEnhancedRequest = CreateTableEnhancedRequest.builder()
        .provisionedThroughput(
            ProvisionedThroughput.builder()
                .readCapacityUnits(10L)
                .writeCapacityUnits(10L)
                .build())
        .build();

    idpInternalUserMapper.createTable(createTableEnhancedRequest);
  }

  @AfterEach
  public void afterEach() {
    idpInternalUserMapper.deleteTable();
  }

  @Test
  void testGetIDPInternalUserByUsernameAndNamespace() {
    // Given
    String username = "testUser";
    String namespace = "testNamespace";
    IDPInternalUser user = IDPInternalUser.builder()
        .username(username)
        .namespace(namespace)
        .build();
    idpInternalUserMapper.putItem(user);

    // When
    var result = internalIDPUsersConnector.getIDPInternalUserByUsernameAndNamespace(username,
        namespace);

    // Then
    assert result.isPresent();
    assert result.get().getUsername().equals(username);
    assert result.get().getNamespace().equals(namespace);
  }

  @Test
  void testGetIDPInternalUserByUsernameAndNamespaceNotFound() {
    // Given
    String username = "testUser";
    String namespace = "testNamespace";

    // When
    var result = internalIDPUsersConnector.getIDPInternalUserByUsernameAndNamespace(username,
        namespace);

    // Then
    assert result.isEmpty();
  }

}
