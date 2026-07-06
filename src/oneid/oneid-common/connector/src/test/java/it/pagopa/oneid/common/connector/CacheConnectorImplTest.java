package it.pagopa.oneid.common.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.mockito.ArgumentCaptor;
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
  @DisplayName("given blank client id when get by client id then return empty")
  void given_blank_client_id_when_get_by_client_id_then_return_empty() {
    Optional<Client> result = cacheConnector.getByClientId(" ");

    assertTrue(result.isEmpty());
    verifyNoInteractions(valueCommands);
  }

  @Test
  @DisplayName("given empty payload when get by client id then return empty")
  void given_empty_payload_when_get_by_client_id_then_return_empty() {
    when(valueCommands.get("oneid:client:v1:client-test")).thenReturn(" ");

    Optional<Client> result = cacheConnector.getByClientId("client-test");

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("given malformed payload when get by client id then throw illegal state")
  void given_malformed_payload_when_get_by_client_id_then_throw_illegal_state() throws Exception {
    ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
    when(failingObjectMapper.readValue(anyString(), eq(Client.class)))
        .thenThrow(new JsonProcessingException("boom") {
        });
    when(valueCommands.get("oneid:client:v1:client-test")).thenReturn("{broken-json}");

    CacheConnectorImpl failingCacheConnector = new CacheConnectorImpl(redisDataSource,
        failingObjectMapper, "oneid:client:v1:");

    assertThrows(IllegalStateException.class,
        () -> failingCacheConnector.getByClientId("client-test"));
  }

  @Test
  @DisplayName("given valid client when set client then write serialized payload")
  void given_valid_client_when_set_client_then_write_serialized_payload() throws Exception {
    Client client = buildClient("client-test");

    cacheConnector.setClient(client);

    ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
    verify(valueCommands).set(eq("oneid:client:v1:client-test"), payloadCaptor.capture());

    Client storedClient = new ObjectMapper().readValue(payloadCaptor.getValue(), Client.class);
    assertEquals("client-test", storedClient.getClientId());
  }

  @Test
  @DisplayName("given null client when set client then skip redis write")
  void given_null_client_when_set_client_then_skip_redis_write() {
    cacheConnector.setClient(null);

    verifyNoInteractions(valueCommands);
    verifyNoInteractions(keyCommands);
  }

  @Test
  @DisplayName("given blank client id when set client then skip redis write")
  void given_blank_client_id_when_set_client_then_skip_redis_write() {
    cacheConnector.setClient(buildClient(" "));

    verifyNoInteractions(valueCommands);
    verifyNoInteractions(keyCommands);
  }

  @Test
  @DisplayName("given serialization failure when set client then throw illegal state")
  void given_serialization_failure_when_set_client_then_throw_illegal_state() throws Exception {
    ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
    when(failingObjectMapper.writeValueAsString(any()))
        .thenThrow(new JsonProcessingException("boom") {
        });

    CacheConnectorImpl failingCacheConnector = new CacheConnectorImpl(redisDataSource,
        failingObjectMapper, "oneid:client:v1:");

    assertThrows(IllegalStateException.class,
        () -> failingCacheConnector.setClient(buildClient("client-test")));
  }

  @Test
  @DisplayName("given client id when delete client then remove redis key")
  void given_client_id_when_delete_client_then_remove_redis_key() {
    cacheConnector.deleteClient("client-test");

    verify(keyCommands).del("oneid:client:v1:client-test");
  }

  @Test
  @DisplayName("given blank client id when delete client then skip redis delete")
  void given_blank_client_id_when_delete_client_then_skip_redis_delete() {
    cacheConnector.deleteClient(" ");

    verifyNoInteractions(keyCommands);
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
