package it.pagopa.oneid.connector;

import static it.pagopa.oneid.connector.utils.ConnectorConstants.VALID_TIME_ACCESS_TOKEN_MIN;
import static it.pagopa.oneid.connector.utils.ConnectorConstants.VALID_TIME_OIDC_MIN;
import static it.pagopa.oneid.connector.utils.ConnectorConstants.VALID_TIME_SAML_MIN;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@Dependent
@CustomLogging
public class SessionConnectorImpl<T extends Session> implements SessionConnector<T> {

  private static final int MAX_RETRIES = 4;
  private static final long RETRY_DELAY_MS = 500;
  private static final long EXPONENTIAL_BACKOFF_FACTOR = 2;
  @Inject
  CloudWatchConnectorImpl cloudWatchConnectorImpl;
  private DynamoDbTable<SAMLSession> samlSessionMapper;
  private DynamoDbTable<OIDCSession> oidcSessionMapper;
  private DynamoDbTable<AccessTokenSession> accessTokenSessionMapper;
  private DynamoDbIndex<OIDCSession> oidcSessionDynamoDbIndex;
  private DynamoDbIndex<AccessTokenSession> accessTokenSessionDynamoDbIndex;

  public SessionConnectorImpl() {

  }

  @Inject
  SessionConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient,
      @ConfigProperty(name = "sessions_table_name")
      String TABLE_NAME, @ConfigProperty(name = "sessions_g_idx") String IDX_NAME) {
    samlSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(SAMLSession.class));
    oidcSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(OIDCSession.class));
    accessTokenSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(AccessTokenSession.class));
    oidcSessionDynamoDbIndex = oidcSessionMapper.index(IDX_NAME);
    accessTokenSessionDynamoDbIndex = accessTokenSessionMapper.index(IDX_NAME);
  }

  private static <T extends Session> boolean checkSessionValidity(
      T session) throws SessionException {
    switch (session) {
      case SAMLSession samlSession -> {
        if (
            samlSession.getCreationTime() < Instant.now()
                .minus(VALID_TIME_SAML_MIN, ChronoUnit.MINUTES)
                .getEpochSecond()) {
          return false;
        }
      }
      case OIDCSession oidcSession -> {
        if (
            oidcSession.getCreationTime() < Instant.now()
                .minus(VALID_TIME_OIDC_MIN, ChronoUnit.MINUTES)
                .getEpochSecond()) {
          return false;
        }
      }
      case AccessTokenSession accessTokenSession -> {
        if (
            accessTokenSession.getCreationTime() < Instant.now()
                .minus(VALID_TIME_ACCESS_TOKEN_MIN, ChronoUnit.MINUTES)
                .getEpochSecond()) {
          return false;
        }
      }
      default -> {
        Log.debug("not valid RecordType");
        throw new SessionException();
      }
    }

    return true;
  }

  private <R> Optional<R> executeWithRetry(Supplier<Optional<R>> operation)
      throws SessionException {
    int attempts = 0;
    long delay = SessionConnectorImpl.RETRY_DELAY_MS;

    while (attempts < SessionConnectorImpl.MAX_RETRIES) {
      Optional<R> result = operation.get();
      if (result.isPresent()) {
        if (attempts > 0) {
          cloudWatchConnectorImpl.sendOIDynamoDBErrorMetricData(attempts + 1);
        }
        return result;
      }

      Log.error(
          "Session not found in DynamoDB at attempt number " + (attempts + 1) + ", retrying...");

      attempts++;
      if (attempts < SessionConnectorImpl.MAX_RETRIES) {
        try {
          Thread.sleep(delay);
          delay *= EXPONENTIAL_BACKOFF_FACTOR;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SessionException();
        }
      }
    }

    return Optional.empty();
  }

  @Override
  public void saveSessionIfNotExists(T session) throws SessionException {
    try {
      saveItemIfNotExists(session);
      Log.debug("successfully saved session");
    } catch (ConditionalCheckFailedException e) {
      Log.error(
          "a record with the same SAMLRequestID already exists");
      throw new SessionException("Existing session for the specified SAMLRequestID");
    }
  }

  private void saveItemIfNotExists(T session)
      throws ConditionalCheckFailedException, SessionException {
    switch (session) {
      case SAMLSession samlSession -> {
        samlSessionMapper.putItem(
            PutItemEnhancedRequest.builder(SAMLSession.class)
                .item(samlSession)
                .conditionExpression(
                    Expression.builder().expression(
                            "attribute_not_exists(samlRequestID)")
                        .build())
                .build());
      }

      case OIDCSession oidcSession -> {
        oidcSessionMapper.putItem(
            PutItemEnhancedRequest.builder(OIDCSession.class)
                .item(oidcSession)
                .conditionExpression(
                    Expression.builder().expression(
                            "attribute_not_exists(samlRequestID)")
                        .build())
                .build());
      }
      case AccessTokenSession accessTokenSession -> {
        accessTokenSessionMapper.putItem(
            PutItemEnhancedRequest.builder(AccessTokenSession.class)
                .item(accessTokenSession)
                .conditionExpression(
                    Expression.builder().expression(
                            "attribute_not_exists(samlRequestID)")
                        .build())
                .build());
      }
      default -> {
        Log.debug("not valid RecordType");
        throw new SessionException();
      }
    }
  }

  @Override
  public Optional<T> findSession(String identifier, RecordType recordType) throws SessionException {

    switch (recordType) {
      case RecordType.SAML -> {
        SAMLSession samlSession;
        samlSession = samlSessionMapper
            .getItem(GetItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(identifier).sortValue(recordType.name())
                    .build())
                .build());

        if (samlSession == null) {
          Log.debug("saml session not found");
          return Optional.empty();
        }
        if (checkSessionValidity(samlSession)) {
          Log.debug("session successfully found");
          return (Optional<T>) Optional.ofNullable(samlSession);
        }
        return Optional.empty();

      }
      case RecordType.OIDC -> {
        return executeWithRetry(() -> {
          QueryConditional queryConditional = QueryConditional.keyEqualTo(
              Key.builder().partitionValue(identifier).build()
          );

          final SdkIterable<Page<OIDCSession>> pagedResult = oidcSessionDynamoDbIndex.query(q -> q
              .queryConditional(queryConditional)
              .limit(1)
          );

          List<OIDCSession> collectedItems = new ArrayList<>();
          pagedResult.stream().forEach(page -> collectedItems.addAll(page.items()));

          if (!collectedItems.isEmpty()) {
            OIDCSession oidcSession = collectedItems.getFirst();
            try {
              if (checkSessionValidity(oidcSession)) {
                Log.debug("session successfully found");
                return Optional.of((T) oidcSession);
              }
            } catch (SessionException e) {
              throw new RuntimeException(e);
            }
          }
          Log.debug("oidc session not found");
          return Optional.empty();
        });
      }
      case RecordType.ACCESS_TOKEN -> {

        QueryConditional queryConditional = QueryConditional.keyEqualTo(
            Key.builder().partitionValue(identifier)
                .build());

        final SdkIterable<Page<AccessTokenSession>> pagedResult = accessTokenSessionDynamoDbIndex.query(
            q -> q
                .queryConditional(queryConditional)
                .limit(1)
        );

        List<AccessTokenSession> collectedItems = new ArrayList<>();
        pagedResult.stream().forEach(page -> collectedItems.addAll(page.items()));
        AccessTokenSession accessTokenSession;
        try {
          accessTokenSession = collectedItems.getFirst();
        } catch (NoSuchElementException e) {
          Log.debug("access token session not found");
          return Optional.empty();
        }
        if (checkSessionValidity(accessTokenSession)) {
          Log.debug("session successfully found");
          return (Optional<T>) Optional.ofNullable(accessTokenSession);
        }
        return Optional.empty();
      }
      default -> {
        Log.debug("not valid RecordType");
        throw new SessionException();
      }
    }
  }

  @Override
  public void updateSAMLSession(String samlRequestID, String SAMLResponse) throws SessionException {

    SAMLSession samlSession = (SAMLSession) findSession(samlRequestID, RecordType.SAML).orElseThrow(
        SessionException::new);

    samlSession.setSAMLResponse(SAMLResponse);
    try {
      samlSessionMapper
          .updateItem(UpdateItemEnhancedRequest.builder(SAMLSession.class)
              .item(samlSession)
              .conditionExpression(Expression.builder()
                  .expression("attribute_not_exists(SAMLResponse)")
                  .build())
              .build());
      Log.debug("session successfully updated");

    } catch (ConditionalCheckFailedException e) {
      Log.error(
          "the record contains already the samlResponse");
      throw new SessionException("Existing SAMLResponse for the specified samlRequestID");
    }
  }
}
