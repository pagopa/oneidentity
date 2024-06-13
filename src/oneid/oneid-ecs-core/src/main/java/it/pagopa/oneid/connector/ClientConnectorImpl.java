package it.pagopa.oneid.connector;

import it.pagopa.oneid.common.Client;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.ArrayList;
import java.util.Optional;

@ApplicationScoped
public class ClientConnectorImpl implements ClientConnector {
    // TODO how to obtain TABLE_NAME
    private static final String TABLE_NAME = "Client";

    private final DynamoDbTable<Client> clientMapper;

    @Inject
    ClientConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        clientMapper = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(Client.class));
    }

    @Override
    public Optional<ArrayList<Client>> findAll() {

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
        return Optional.empty();
    }
}
