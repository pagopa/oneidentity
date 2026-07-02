package it.pagopa.oneid.common.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CacheConnectorImplTest {

  @Mock
  RedisDataSource redisDataSource;

  @Mock
  ValueCommands<String, String> valueCommands;

  @Mock
  KeyCommands<String> keyCommands;

  CacheConnectorImpl cacheConnector;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(redisDataSource.value(String.class)).thenReturn(valueCommands);
    when(redisDataSource.key()).thenReturn(keyCommands);
    cacheConnector = new CacheConnectorImpl(redisDataSource, new ObjectMapper(),
        "oneid:client:v1:");
  }

  @Test
  @DisplayName("given cached client json when get by client id then deserialize into Client")
  void given_cached_client_json_when_get_by_client_id_then_deserialize_into_client()
      throws Exception {
    Client client = buildClient("client-test");
    String payload = new ObjectMapper().writeValueAsString(client);
    when(valueCommands.get("oneid:client:v1:client-test")).thenReturn(payload);

    Optional<Client> result = cacheConnector.getByClientId("client-test");

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
  }

  @Test
  @DisplayName("given client id when delete client then remove redis key")
  void given_client_id_when_delete_client_then_remove_redis_key() {
    cacheConnector.deleteClient("client-test");

    verify(keyCommands).del("oneid:client:v1:client-test");
  }

  private Client buildClient(String clientId) {
    return Client.builder()
        .clientId(clientId)
        .userId("user-test")
        .friendlyName("friendly")
        .callbackURI(Set.of("https://callback.test"))
        .requestedParameters(Set.of("fiscalNumber"))
        .authLevel(AuthLevel.L2)
        .acsIndex(1)
        .attributeIndex(1)
        .isActive(true)
        .clientIdIssuedAt(1L)
        .build();
  }
}
