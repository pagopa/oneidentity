package it.pagopa.oneid.connector;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

@ApplicationScoped
@CustomLogging
public class CognitoConnectorImpl implements CognitoConnector {

  private static final String CLIENT_ID_ATTRIBUTE = "custom:client_id";

  private final CognitoIdentityProviderClient cognitoClient;
  private final String userPoolId;

  @Inject
  public CognitoConnectorImpl(
      CognitoIdentityProviderClient cognitoClient,
      @ConfigProperty(name = "cognito_user_pool_id") String userPoolId) {
    this.cognitoClient = cognitoClient;
    this.userPoolId = userPoolId;
  }

  public Optional<String> extractClientIdByUserId(String userId) {
    try {
      Log.debugf("Attempting to extract client ID for user: %s", userId);
      AdminGetUserRequest getUserRequest = AdminGetUserRequest.builder()
          .userPoolId(userPoolId)
          .username(userId)
          .build();

      AdminGetUserResponse getUserResponse = cognitoClient.adminGetUser(getUserRequest);

      Log.debugf("Successfully retrieved user data for: %s", userId);

      // Search for client_id in user attributes
      Optional<String> clientId = getUserResponse.userAttributes()
          .stream()
          .filter(attribute -> CLIENT_ID_ATTRIBUTE.equalsIgnoreCase(attribute.name()))
          .map(AttributeType::value)
          .filter(value -> value != null && !value.trim().isEmpty())
          .findFirst();

      if (clientId.isPresent()) {
        Log.infof("Client ID found for user %s: %s", userId, clientId.get());
      } else {
        Log.warnf("No client ID attribute found for user: %s", userId);
      }

      return clientId;

    } catch (UserNotFoundException e) {
      Log.warnf("User not found in Cognito User Pool: %s", userId);
      return Optional.empty();

    } catch (CognitoIdentityProviderException e) {
      Log.errorf(e, "Cognito service error while retrieving user %s: %s",
          userId, e.getMessage());
      return Optional.empty();

    } catch (Exception e) {
      Log.errorf(e, "Unexpected error while extracting client ID for user %s: %s",
          userId, e.getMessage());
      return Optional.empty();
    }
  }


}
