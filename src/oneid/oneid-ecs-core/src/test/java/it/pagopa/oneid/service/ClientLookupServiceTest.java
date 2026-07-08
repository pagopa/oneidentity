package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import it.pagopa.oneid.common.connector.CacheConnector;
import it.pagopa.oneid.common.connector.ClientConnector;
import it.pagopa.oneid.common.model.Client;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.sns.SnsClient;

@QuarkusTest
class ClientLookupServiceTest {

  @Inject
  ClientLookupServiceImpl clientLookupService;

  @InjectMock
  CacheConnector cacheConnector;

  @InjectMock
  ClientConnector clientConnector;

  @InjectMock
  SnsClient sns;

  @Test
  @DisplayName("given cache hit when reading client then return cached value without dynamo call")
  void given_cache_hit_when_reading_client_then_return_cached_value() {
    Client cachedClient = Client.builder().clientId("client-test").build();
    when(cacheConnector.getByClientId("client-test")).thenReturn(Optional.of(cachedClient));

    Optional<Client> result = clientLookupService.getClientById("client-test");

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
    verify(cacheConnector).getByClientId("client-test");
    verify(clientConnector, never()).getClientById("client-test");
    verify(sns, never()).publish(Mockito.any(
        software.amazon.awssdk.services.sns.model.PublishRequest.class));
  }

  @Test
  @DisplayName("given cache miss when reading client then fallback to dynamodb and backfill cache")
  void given_cache_miss_when_reading_client_then_fallback_to_dynamodb_and_backfill_cache() {
    Client sourceClient = Client.builder().clientId("client-test").build();
    when(cacheConnector.getByClientId("client-test")).thenReturn(Optional.empty());
    when(clientConnector.getClientById("client-test")).thenReturn(Optional.of(sourceClient));

    Optional<Client> result = clientLookupService.getClientById("client-test");

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
    verify(cacheConnector).getByClientId("client-test");
    verify(clientConnector).getClientById("client-test");
    verify(cacheConnector).setClient(sourceClient);
  }

  @Test
  @DisplayName("given cache and dynamodb miss when reading client then return empty")
  void given_cache_and_dynamodb_miss_when_reading_client_then_return_empty() {
    when(cacheConnector.getByClientId("client-missing")).thenReturn(Optional.empty());
    when(clientConnector.getClientById("client-missing")).thenReturn(Optional.empty());

    Optional<Client> result = clientLookupService.getClientById("client-missing");

    assertTrue(result.isEmpty());
    verify(cacheConnector).getByClientId("client-missing");
    verify(clientConnector).getClientById("client-missing");
    verify(sns, never()).publish(Mockito.any(
        software.amazon.awssdk.services.sns.model.PublishRequest.class));
  }

  @Test
  @DisplayName("given dynamo fallback when reading client then publish sns notification")
  void given_dynamo_fallback_when_reading_client_then_publish_sns_notification() {
    Client sourceClient = Client.builder().clientId("client-test").build();
    when(cacheConnector.getByClientId("client-test")).thenReturn(Optional.empty());
    when(clientConnector.getClientById("client-test")).thenReturn(Optional.of(sourceClient));

    clientLookupService.getClientById("client-test");

    verify(sns, Mockito.times(2)).publish(
        Mockito.<java.util.function.Consumer<software.amazon.awssdk.services.sns.model.PublishRequest.Builder>>any());
  }

  @Test
  @DisplayName("given cache read failure when reading client then fallback to dynamodb")
  void given_cache_read_failure_when_reading_client_then_fallback_to_dynamodb() {
    Client sourceClient = Client.builder().clientId("client-test").build();
    when(cacheConnector.getByClientId("client-test")).thenThrow(new RuntimeException("Redis down"));
    when(clientConnector.getClientById("client-test")).thenReturn(Optional.of(sourceClient));

    Optional<Client> result = clientLookupService.getClientById("client-test");

    assertTrue(result.isPresent());
    verify(clientConnector).getClientById("client-test");
  }
}
