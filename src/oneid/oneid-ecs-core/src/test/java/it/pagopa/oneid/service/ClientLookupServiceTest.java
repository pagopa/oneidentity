package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import it.pagopa.oneid.common.connector.CacheConnector;
import it.pagopa.oneid.common.connector.ClientConnector;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ClientLookupServiceTest {

  @Inject
  ClientLookupServiceImpl clientLookupService;

  @InjectMock
  CacheConnector cacheConnector;

  @InjectMock
  ClientConnector clientConnector;

  @InjectMock
  CloudWatchConnectorImpl cloudWatchConnector;

  @Test
  @DisplayName("given cache hit when reading client then return cached value without dynamo call")
  void given_cache_hit_when_reading_client_then_return_cached_value() {
    Client cachedClient = Client.builder().clientId("client-test").isActive(true).build();
    when(cacheConnector.getByClientId("client-test")).thenReturn(Optional.of(cachedClient));

    Optional<Client> result = clientLookupService.getClientById("client-test");

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
    verify(cacheConnector).getByClientId("client-test");
    verify(clientConnector, never()).getClientById("client-test");
  }

  @Test
  @DisplayName("given cache miss when reading client then fallback to dynamodb and backfill cache")
  void given_cache_miss_when_reading_client_then_fallback_to_dynamodb_and_backfill_cache() {
    Client sourceClient = Client.builder().clientId("client-test").isActive(true).build();
    when(cacheConnector.getByClientId("client-test")).thenReturn(Optional.empty());
    when(clientConnector.getClientById("client-test")).thenReturn(Optional.of(sourceClient));

    Optional<Client> result = clientLookupService.getClientById("client-test");

    assertTrue(result.isPresent());
    assertEquals("client-test", result.get().getClientId());
    verify(cacheConnector).getByClientId("client-test");
    verify(clientConnector).getClientById("client-test");
    verify(cacheConnector).setClient(sourceClient);
    verify(cloudWatchConnector).sendClientCacheMissMetricData("client-test");
    verify(cloudWatchConnector).sendClientCacheBackfillSuccessMetricData("client-test");
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
    verifyNoInteractions(cloudWatchConnector);
  }

  @Test
  @DisplayName("given cache write failure when reading client then emit backfill failure metric")
  void given_cache_write_failure_when_reading_client_then_emit_backfill_failure_metric() {
    Client sourceClient = Client.builder().clientId("client-test").isActive(true).build();
    when(cacheConnector.getByClientId("client-test")).thenReturn(Optional.empty());
    when(clientConnector.getClientById("client-test")).thenReturn(Optional.of(sourceClient));
    doThrow(new RuntimeException("Redis write failed")).when(cacheConnector).setClient(sourceClient);

    Optional<Client> result = clientLookupService.getClientById("client-test");

    assertTrue(result.isPresent());
    verify(cloudWatchConnector).sendClientCacheMissMetricData("client-test");
    verify(cloudWatchConnector).sendClientCacheBackfillFailureMetricData("client-test");
  }

  @Test
  @DisplayName("given cache read failure when reading client then fallback to dynamodb")
  void given_cache_read_failure_when_reading_client_then_fallback_to_dynamodb() {
    Client sourceClient = Client.builder().clientId("client-test").isActive(true).build();
    when(cacheConnector.getByClientId("client-test")).thenThrow(new RuntimeException("Redis down"));
    when(clientConnector.getClientById("client-test")).thenReturn(Optional.of(sourceClient));

    Optional<Client> result = clientLookupService.getClientById("client-test");

    assertTrue(result.isPresent());
    verify(clientConnector).getClientById("client-test");
    verify(cloudWatchConnector).sendClientCacheMissMetricData("client-test");
    verify(cloudWatchConnector).sendClientCacheBackfillSuccessMetricData("client-test");
  }

  @Test
  @DisplayName("given inactive dynamodb client on cache miss when reading then do not backfill")
  void given_inactive_dynamodb_client_on_cache_miss_when_reading_then_do_not_backfill() {
    Client inactiveClient = Client.builder().clientId("client-test").isActive(false).build();
    when(cacheConnector.getByClientId("client-test")).thenReturn(Optional.empty());
    when(clientConnector.getClientById("client-test")).thenReturn(Optional.of(inactiveClient));

    Optional<Client> result = clientLookupService.getClientById("client-test");

    assertTrue(result.isEmpty());
    verify(cacheConnector).getByClientId("client-test");
    verify(clientConnector).getClientById("client-test");
    verify(cacheConnector, never()).setClient(inactiveClient);
    verifyNoInteractions(cloudWatchConnector);
  }
}
