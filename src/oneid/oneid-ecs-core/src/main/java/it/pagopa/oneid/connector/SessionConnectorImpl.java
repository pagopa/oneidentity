package it.pagopa.oneid.connector;

import static it.pagopa.oneid.connector.utils.ConnectorConstants.VALID_TIME_SAML;
import io.quarkus.logging.Log;
import it.pagopa.oneid.exception.SessionException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.OIDCSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.Session;
import it.pagopa.oneid.model.session.enums.RecordType;
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
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

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
    try {
      saveItem(session);
      Log.debug("[SessionConnectorImpl.saveSession] successfully saved session");
    } catch (ConditionalCheckFailedException e) {
      Log.debug(
          "[SessionConnectorImpl.saveSession] a record with the same SAMLRequestID already exists");
      throw new SessionException("Existing session for the specified SAMLRequestID");
    }
  }

  private void saveItem(T session)
      throws ConditionalCheckFailedException, SessionException {
    switch (session) {
      case SAMLSession samlSession -> {
        samlSessionMapper.putItem(
            PutItemEnhancedRequest.builder(SAMLSession.class)
                .item(samlSession)
                .conditionExpression(
                    Expression.builder().expression(
                            "attribute_not_exists(SAMLRequestID)")
                        .build())
                .build());
      }
      case OIDCSession oidcSession -> {
        oidcSessionMapper.putItem(
            PutItemEnhancedRequest.builder(OIDCSession.class)
                .item(oidcSession)
                .conditionExpression(
                    Expression.builder().expression(
                            "attribute_not_exists(SAMLRequestID)")
                        .build())
                .build());
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
  public Optional<T> findSession(String identifier, RecordType recordType) throws SessionException {
    Log.debug("[SessionConnectorImpl.findSession] start");

    switch (recordType) {
      case RecordType.SAML -> {
        SAMLSession samlSession;
        samlSession = samlSessionMapper
            .getItem(GetItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(identifier).sortValue(recordType.name())
                    .build())
                .consistentRead(true)
                .build());

        if (samlSession == null) {
          Log.debug("[SessionConnectorImpl.findSession] session not found");
          return Optional.empty();
        }
        if (
            samlSession.getCreationTime() < Instant.now()
                .minus(VALID_TIME_SAML, ChronoUnit.MINUTES)
                .getEpochSecond()) {
          Log.debug("[SessionConnectorImpl.findSession] session expired");
          return Optional.empty();
        }

        Log.debug("[SessionConnectorImpl.findSession] session successfully found");
        return (Optional<T>) Optional.ofNullable(samlSession);
      }
      case RecordType.OIDC -> {
        return Optional.empty();
      }
      case RecordType.ACCESS_TOKEN -> {
        return Optional.empty();
      }
      default -> {
        Log.debug("[SessionConnectorImpl.findSession] not valid RecordType");
        throw new SessionException();
      }
    }
  }

  @Override
  public void updateSAMLSession(String samlRequestID, String SAMLResponse) throws SessionException {
    Log.debug("[SessionConnectorImpl.updateSAMLSession] invoked");

    SAMLSession samlSession = null;
    try {
      samlSession = (SAMLSession) findSession(samlRequestID, RecordType.SAML).get();
    } catch (SessionException e) {
      Log.debug("[SessionConnectorImpl.updateSAMLSession] session to update not found");
    }

    samlSession.setSAMLResponse(SAMLResponse);
    try {
      samlSessionMapper
          .updateItem(UpdateItemEnhancedRequest.builder(SAMLSession.class)
              .item(samlSession)
              .conditionExpression(Expression.builder()
                  .expression("attribute_not_exists(SAMLResponse)")
                  .build())
              .build());
      Log.debug("[SessionConnectorImpl.updateSAMLSession] session successfully updated");

    } catch (ConditionalCheckFailedException e) {
      Log.debug(
          "[SessionConnectorImpl.updateSAMLSession] the record contains already the samlResponse");
      throw new SessionException("Existing SAMLResponse for the specified samlRequestID");
    }
  }
}
