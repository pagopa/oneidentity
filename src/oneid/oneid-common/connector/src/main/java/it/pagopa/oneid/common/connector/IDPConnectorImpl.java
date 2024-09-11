package it.pagopa.oneid.common.connector;

import static software.amazon.awssdk.enhanced.dynamodb.internal.AttributeValues.stringValue;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

@ApplicationScoped
public class IDPConnectorImpl implements IDPConnector {

  private final DynamoDbTable<IDP> idpMapper;


  @Inject
  IDPConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient,
      @ConfigProperty(name = "idp_table_name") String TABLE_NAME) {
    idpMapper = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(IDP.class));
  }

  // TODO: check how we can improve this method usability
  @Override
  public Optional<ArrayList<IDP>> findIDPsByTimestamp(String timestamp) {
    ArrayList<IDP> idps = new ArrayList<>();

    // TODO: if we want to avoid a Scan on this table we should add an index on the pointer field
    ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest
        .builder()
        .filterExpression(Expression.builder()
            .expression("pointer = :pointer")
            .expressionValues(Map.of(":pointer", stringValue(timestamp)))
            .build())
        .build();

    PageIterable<IDP> pagedResults = idpMapper.scan(scanEnhancedRequest);

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
  public Optional<IDP> getIDPByEntityIDAndTimestamp(String entityID, String timestamp) {
    return Optional.ofNullable(idpMapper.getItem(
        Key.builder().partitionValue(entityID).sortValue(timestamp).build()));
  }

  @Override
  public void saveIDPs(ArrayList<IDP> idpList, LatestTAG latestTAG) {
    Log.debug("start");

    Optional<ArrayList<IDP>> idpLatestList = findIDPsByTimestamp(String.valueOf(latestTAG));

    // update old latestTag idps with new pointer
    idpLatestList.ifPresent(idps -> {
      idps.forEach(idp -> {
        idpMapper.deleteItem(idp);
        idp.setPointer(String.valueOf(idp.getTimestamp()));
        idpMapper.putItem(idp);
      });

    });

    // put new items on dynamodb table
    idpList.forEach(idpMapper::putItem);

  }
}
