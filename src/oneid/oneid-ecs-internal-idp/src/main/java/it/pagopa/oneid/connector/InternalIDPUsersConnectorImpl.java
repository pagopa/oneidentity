package it.pagopa.oneid.connector;

import it.pagopa.oneid.model.IDPInternalUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@ApplicationScoped
public class InternalIDPUsersConnectorImpl implements InternalIDPUsersConnector {

  private final DynamoDbTable<IDPInternalUser> idpInternalUserMapper;


  @Inject
  InternalIDPUsersConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient,
      @ConfigProperty(name = "idp_internal_users_table_name") String TABLE_NAME) {
    idpInternalUserMapper = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(
        IDPInternalUser.class));
  }

  @Override
  public Optional<IDPInternalUser> getIDPInternalUserByUsernameAndNamespace(String username,
      String namespace) {
    IDPInternalUser idpInternalUser = idpInternalUserMapper.getItem(
        IDPInternalUser.builder()
            .username(username)
            .namespace(namespace)
            .build());
    return Optional.ofNullable(idpInternalUser);
  }
}
