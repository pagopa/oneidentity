package it.pagopa.oneid.common.connector;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.dto.SecretDTO;
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
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

@ApplicationScoped
public class ClientConnectorImpl implements ClientConnector {

  private final DynamoDbTable<Client> clientMapper;
  private final DynamoDbTable<ClientExtended> clientExtendedMapper;


  @Inject
  ClientConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient,
      @ConfigProperty(name = "client_registrations_table_name") String TABLE_NAME) {
    clientMapper = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(Client.class));
    clientExtendedMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(ClientExtended.class));
  }

  @Override
  public Optional<ArrayList<Client>> findAll() {
    Log.debug("[ClientConnectorImpl.findAll] start");
    ArrayList<Client> clients = new ArrayList<>();
    ScanEnhancedRequest request = ScanEnhancedRequest.builder()
        .build();
    PageIterable<Client> pagedResults = clientMapper.scan(request);
    pagedResults.items().stream()
        .forEach(
            clients::add
        );

    if (!clients.isEmpty()) {
      return Optional.of(clients);
    }
    Log.debug("[ClientConnectorImpl.findAll] table Client is empty");
    return Optional.empty();
  }

  @Override
  public Optional<SecretDTO> getClientSecret(String clientId) {
    Log.debug("[ClientConnectorImpl.getClientSecret] start");
    return Optional.of(
        clientExtendedMapper.getItem(Key.builder().partitionValue(clientId).build())).map(
        ClientExtended::clientSecretDTO
    );
  }
}
