package it.pagopa.oneid.common.connector;

import it.pagopa.oneid.common.model.LastIDPUsed;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

@ApplicationScoped
@CustomLogging
public class LastIDPUsedConnectorImpl implements LastIDPUsedConnector {

  private final DynamoDbTable<LastIDPUsed> lastIDPUsedMapper;


  @Inject
  LastIDPUsedConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient,
      @ConfigProperty(name = "last_idp_used_table_name") String TABLE_NAME) {
    lastIDPUsedMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(LastIDPUsed.class));
  }

  @Override
  public Optional<LastIDPUsed> findLastIDPUsed(String id, String clientId) {
    return Optional.ofNullable(lastIDPUsedMapper.getItem(
        Key.builder().partitionValue(id).sortValue(clientId).build()));
  }

  @Override
  public void updateLastIDPUsed(LastIDPUsed lastIDPUsed) {
    lastIDPUsedMapper.putItem(
        PutItemEnhancedRequest.builder(LastIDPUsed.class)
            .item(lastIDPUsed)
            .build());

  }
}
