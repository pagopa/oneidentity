package it.pagopa.oneid.connector;

import io.quarkus.logging.Log;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.OIDCSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.Session;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

@Dependent
public class SessionConnectorImpl<T extends Session> implements SessionConnector<T> {

  // TODO how to obtain TABLE_NAME and GSI_IDX_NAME
  private static final String TABLE_NAME = "Session";
  private static final String GSI_IDX_NAME = "gsi_code_idx";


  private final DynamoDbTable<SAMLSession> samlSessionMapper;
  private final DynamoDbTable<OIDCSession> oidcSessionMapper;
  private final DynamoDbTable<AccessTokenSession> accessTokenSessionMapper;

  @Inject
  SessionConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
    samlSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(SAMLSession.class));
    oidcSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(OIDCSession.class));
    accessTokenSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(AccessTokenSession.class));
  }

  @Override
  public void saveSession(T session) throws SessionException {
    Log.debug("[SessionConnectorImpl.saveSession] start");
    // TODO: find a better way to avoid duplication
    switch (session) {
      case SAMLSession samlSession -> {
        try {
          samlSessionMapper.putItem(
              PutItemEnhancedRequest.builder(SAMLSession.class)
                  .item(samlSession)
                  .conditionExpression(
                      Expression.builder().expression(
                              "attribute_not_exists(SAMLRequestID)")
                          .build())
                  .build());
        } catch (ConditionalCheckFailedException e) {
          Log.debug(
              "[SessionConnectorImpl.saveSession] a record with the same SAMLRequestID already exists");
          throw new SessionException("Existing SAMLSession for the specified SAMLRequestID");
        }
        Log.debug("[SessionConnectorImpl.saveSession] successfully saved SamlSession");
      }

      case OIDCSession oidcSession -> {
        try {
          oidcSessionMapper.putItem(
              PutItemEnhancedRequest.builder(OIDCSession.class)
                  .item(oidcSession)
                  .conditionExpression(
                      Expression.builder().expression(
                              "attribute_not_exists(SAMLRequestID)")
                          .build())
                  .build());

        } catch (ConditionalCheckFailedException e) {
          Log.debug(
              "[SessionConnectorImpl.saveSession] a record with the same SAMLRequestID already exists");
          throw new SessionException("Existing OIDCSession for the specified SAMLRequestID");
        }
        Log.debug("[SessionConnectorImpl.saveSession] successfully saved OIDCSession");
      }
      case AccessTokenSession accessTokenSession -> {
        //TODO implement
        break;
      }
      default -> {
        throw new SessionException();
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
