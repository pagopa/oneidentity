package it.pagopa.oneid.common.connector;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.LatestItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@ApplicationScoped
public class IDPConnectorImpl implements IDPConnector {

  private final DynamoDbTable<IDP> idpMapper;
  private final DynamoDbTable<LatestItem> latestItemMapper;


  @Inject
  IDPConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient,
      @ConfigProperty(name = "idp_table_name") String TABLE_NAME) {
    idpMapper = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(IDP.class));
    latestItemMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(LatestItem.class));
  }

  // TODO: check how we can improve this method usability
  @Override
  public Optional<ArrayList<IDP>> findIDPsByTimestamp(long timestamp) {
    ArrayList<IDP> idps = new ArrayList<>();

    QueryConditional queryConditional = QueryConditional
        .keyEqualTo(Key
            .builder()
            .sortValue(timestamp)
            .build());

    QueryEnhancedRequest queryEnhancedRequest =
        QueryEnhancedRequest
            .builder()
            .queryConditional(queryConditional)
            .build();

    PageIterable<IDP> pagedResults = idpMapper.query(queryEnhancedRequest);

    pagedResults.items().stream()
        .forEach(
            idps::add
        );

    if (!idps.isEmpty()) {
      return Optional.of(idps);
    }
    Log.debug("IDPs not found");
    return Optional.empty();
  }

  @Override
  public Optional<IDP> getLatestIDPVersionByEntityIDAndTimestamp(String entityID, long timestamp) {
    return Optional.of(idpMapper.getItem(
        Key.builder().partitionValue(entityID).sortValue(timestamp).build()));
  }

  @Override
  public void saveIDPs(ArrayList<IDP> idpList, LatestItem updatedLatestItem) {
    Log.debug("start");

    // put items on dynamodb table
    idpList.forEach(idpMapper::putItem);

    // update LatestItem pointer
    latestItemMapper.updateItem(updatedLatestItem);
  }

  @Override
  public Optional<LatestItem> getLatestItem() {
    return Optional.of(
        latestItemMapper.getItem(
            Key.builder().partitionValue(LatestItem.LATEST_ITEM_PK)
                .sortValue(LatestItem.LATEST_ITEM_RK).build()));
  }
}
