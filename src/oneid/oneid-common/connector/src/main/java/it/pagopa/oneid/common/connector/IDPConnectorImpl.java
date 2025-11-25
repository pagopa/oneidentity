package it.pagopa.oneid.common.connector;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@ApplicationScoped
@CustomLogging
public class IDPConnectorImpl implements IDPConnector {

  private final DynamoDbTable<IDP> idpMapper;
  private final DynamoDbIndex<IDP> idpIndexMapper;


  @Inject
  IDPConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient,
      @ConfigProperty(name = "idp_table_name") String TABLE_NAME,
      @ConfigProperty(name = "idp_g_idx") String IDX_NAME) {
    idpMapper = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(IDP.class));
    idpIndexMapper = idpMapper.index(IDX_NAME);
  }

  // TODO: check how we can improve this method usability
  @Override
  public Optional<ArrayList<IDP>> findIDPsByTimestamp(String timestamp) {
    ArrayList<IDP> idps = new ArrayList<>();

    QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest
        .builder()
        .queryConditional(
            QueryConditional.keyEqualTo(Key.builder().partitionValue(timestamp).build()))
        .build();

    SdkIterable<Page<IDP>> pagedResults = idpIndexMapper.query(queryEnhancedRequest);

    pagedResults.stream().forEach(page -> idps.addAll(page.items()));

    if (!idps.isEmpty()) {
      return Optional.of(idps);
    }
    Log.warn("IDPs not found for timestamp: " + timestamp);
    return Optional.empty();
  }

  @Override
  public Optional<IDP> getIDPByEntityIDAndTimestamp(String entityID, String timestamp) {
    return Optional.ofNullable(idpMapper.getItem(
        Key.builder().partitionValue(entityID).sortValue(timestamp).build()));
  }

  @Override
  public void saveIDPs(ArrayList<IDP> idpList, LatestTAG latestTAG, String timestamp) {

    Optional<ArrayList<IDP>> idpLatestList = findIDPsByTimestamp(String.valueOf(latestTAG));

    // update old latestTag idps with new pointer
    idpLatestList.ifPresent(idps -> {
      idps.forEach(idp -> {
        idpMapper.deleteItem(idp);
        idp.setPointer(timestamp);
        idpMapper.putItem(idp);
      });

    });

    // put new items on dynamodb table
    idpList.forEach(idpMapper::putItem);

  }
}
