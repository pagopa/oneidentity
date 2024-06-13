package it.pagopa.oneid.connector;

import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.OIDCSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.Session;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Dependent
public class SessionConnectorImpl<T extends Session> implements SessionConnector<T> {

    // TODO how to obtain TABLE_NAME and GSI_IDX_NAME
    private static final String TABLE_NAME = "Session";
    private static final String GSI_IDX_NAME = "gsi_code_idx";


    private final DynamoDbTable<OIDCSession> oidcSessionMapper;
    private final DynamoDbTable<AccessTokenSession> accessTokenSessionMapper;
    private final DynamoDbTable<SAMLSession> samlSessionMapper;

    @Inject
    SessionConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        samlSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(SAMLSession.class));
        oidcSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(OIDCSession.class));
        accessTokenSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(AccessTokenSession.class));
    }

    @Override
    public void saveSession(T session) {
        switch (session) {
            case SAMLSession samlSession -> samlSessionMapper.putItem(samlSession);
            case OIDCSession oidcSession -> {
                //TODO implement
                break;
            }
            case AccessTokenSession accessTokenSession -> {
                //TODO implement
                break;
            }
            default -> {
                return;
            }
        }
    }

    @Override
    public Optional<T> findSession(String identifier) {
        return Optional.empty();
    }

    @Override
    public void updateSAMLSession(String samlRequestID, String SAMLResponse) {
    }
}
