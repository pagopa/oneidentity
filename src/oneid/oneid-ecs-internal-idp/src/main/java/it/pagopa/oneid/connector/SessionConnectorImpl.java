package it.pagopa.oneid.connector;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.model.IDPSession;
import it.pagopa.oneid.model.enums.IDPSessionStatus;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest.Builder;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@Dependent
@CustomLogging
public class SessionConnectorImpl implements SessionConnector {

  private final DynamoDbTable<IDPSession> idpSessionMapper;

  public SessionConnectorImpl(DynamoDbTable<IDPSession> idpSessionMapper) {
    this.idpSessionMapper = idpSessionMapper;
  }

  @Inject
  SessionConnectorImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient,
      @ConfigProperty(name = "idp_sessions_table_name") String TABLE_NAME) {
    idpSessionMapper = dynamoDbEnhancedClient.table(TABLE_NAME,
        TableSchema.fromBean(IDPSession.class));

  }

  @Override
  public void saveIDPSessionIfNotExists(IDPSession idpSession)
      throws ConditionalCheckFailedException, OneIdentityException {
    Log.debug(
        "Start saveIDPSessionIfNotExists for authnRequestId: " + idpSession.getAuthnRequestId());
    try {
      idpSessionMapper.putItem(
          PutItemEnhancedRequest.builder(IDPSession.class)
              .item(idpSession)
              .conditionExpression(
                  Expression.builder().expression(
                          "attribute_not_exists(authnRequestId)")
                      .build())
              .build());
      Log.debug(
          "End saveIDPSessionIfNotExists for authnRequestId: " + idpSession.getAuthnRequestId());
    } catch (ConditionalCheckFailedException e) {
      Log.error("ConditionalCheckFailedException in saveIDPSessionIfNotExists for authnRequestId: "
          + idpSession.getAuthnRequestId() + ", error: " + e.getMessage());
      throw new OneIdentityException();
    } catch (Exception e) {
      Log.error("Exception in saveIDPSessionIfNotExists for authnRequestId: "
          + idpSession.getAuthnRequestId() + ", error: " + e.getMessage());
      throw new OneIdentityException();
    }
  }

  @Override
  public Optional<IDPSession> getIDPSessionByAuthnRequestIdClientIdAndUsername(
      String authnRequestId,
      String clientId,
      String username) {

    IDPSession idpSession = idpSessionMapper
        .getItem(
            GetItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(authnRequestId).sortValue(clientId).build())
                .build());

    return (idpSession != null && idpSession.getUsername().equals(username))
        ? Optional.of(idpSession)
        : Optional.empty();
  }

  @Override
  public Optional<IDPSession> findIDPSessionByAuthnRequestIdAndClientId(String authnRequestId,
      String clientId) {
    return Optional.ofNullable(
        idpSessionMapper.getItem(
            Key.builder().partitionValue(authnRequestId).sortValue(clientId).build()));
  }

  @Override
  public void updateIDPSession(IDPSession idpSession,
      Optional<IDPSessionStatus> previousStatus) {

    // Update the IDPSession in the DynamoDB table checking the previous status if provided.

    Builder<IDPSession> builder = UpdateItemEnhancedRequest
        .builder(IDPSession.class)
        .item(idpSession);

    previousStatus.ifPresent(ps -> builder.conditionExpression(
        Expression.builder()
            .expression("#previousStatus = :status")
            .putExpressionName("#previousStatus",
                "status")
            .putExpressionValue(":status",
                AttributeValue.builder()
                    .s(ps.toString())
                    .build()).build()));

    idpSessionMapper.updateItem(builder.build());


  }

}
