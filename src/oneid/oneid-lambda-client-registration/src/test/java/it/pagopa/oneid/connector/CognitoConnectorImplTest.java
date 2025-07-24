package it.pagopa.oneid.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

@QuarkusTest
public class CognitoConnectorImplTest {

  private final String userPoolId = "test-pool";
  private CognitoIdentityProviderClient cognitoClient;
  private CognitoConnectorImpl cognitoConnector;

  @BeforeEach
  void setUp() {
    cognitoClient = mock(CognitoIdentityProviderClient.class);
    cognitoConnector = new CognitoConnectorImpl(cognitoClient, userPoolId);
  }

  @Test
  void extractClientIdByUserId_success() {
    String userId = "user1";
    String clientId = "client-123";
    AdminGetUserResponse response = AdminGetUserResponse.builder()
        .userAttributes(AttributeType.builder().name("custom:client_id").value(clientId).build())
        .build();
    when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class))).thenReturn(response);

    Optional<String> result = cognitoConnector.extractClientIdByUserId(userId);
    assertTrue(result.isPresent());
    assertEquals(clientId, result.get());
  }

  @Test
  void extractClientIdByUserId_noClientIdAttribute() {
    String userId = "user2";
    AdminGetUserResponse response = AdminGetUserResponse.builder()
        .userAttributes(AttributeType.builder().name("other_attribute").value("value").build())
        .build();
    when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class))).thenReturn(response);

    Optional<String> result = cognitoConnector.extractClientIdByUserId(userId);
    assertFalse(result.isPresent());
  }

  @Test
  void extractClientIdByUserId_userNotFound() {
    String userId = "user3";
    when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class))).thenThrow(
        UserNotFoundException.builder().message("User not found").build());
    Optional<String> result = cognitoConnector.extractClientIdByUserId(userId);
    assertFalse(result.isPresent());
  }

  @Test
  void extractClientIdByUserId_cognitoException() {
    String userId = "user4";
    when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class))).thenThrow(
        CognitoIdentityProviderException.builder().message("Service error").build());
    Optional<String> result = cognitoConnector.extractClientIdByUserId(userId);
    assertFalse(result.isPresent());
  }
}
