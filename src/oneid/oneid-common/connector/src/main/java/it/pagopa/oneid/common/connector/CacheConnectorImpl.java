package it.pagopa.oneid.common.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import it.pagopa.oneid.common.model.Client;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class CacheConnectorImpl implements CacheConnector {

  private final ValueCommands<String, String> valueCommands;
  private final KeyCommands<String> keyCommands;
  private final ObjectMapper objectMapper;
  private final String cacheKeyPrefix;
  private final long cacheTtlSeconds;

  @Inject
  CacheConnectorImpl(
      RedisDataSource redisDataSource,
      ObjectMapper objectMapper,
      @ConfigProperty(name = "cache.key-prefix") String cacheKeyPrefix,
      @ConfigProperty(name = "cache.ttl.seconds") long cacheTtlSeconds
  ) {
    this.valueCommands = redisDataSource.value(String.class);
    this.keyCommands = redisDataSource.key();
    this.objectMapper = objectMapper;
    this.cacheKeyPrefix = cacheKeyPrefix;
    this.cacheTtlSeconds = cacheTtlSeconds;
  }

  @Override
  public Optional<Client> getByClientId(String clientId) {
    if (StringUtils.isBlank(clientId)) {
      return Optional.empty();
    }

    String payload = valueCommands.get(cacheKey(clientId));
    if (payload==null || payload.isBlank()) {
      return Optional.empty();
    }

    try {
      return Optional.of(objectMapper.readValue(payload, Client.class));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to deserialize client from Redis cache", e);
    }
  }

  @Override
  public void setClient(Client client) {
    if (client == null || StringUtils.isBlank(client.getClientId())) {
      return;
    }

    String key = cacheKey(client.getClientId());
    String payload;
    try {
      payload = objectMapper.writeValueAsString(client);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize client for Redis cache", e);
    }

    valueCommands.set(key, payload, new SetArgs().ex(cacheTtlSeconds));
  }

  @Override
  public void deleteClient(String clientId) {
    if (StringUtils.isBlank(clientId)) {
      return;
    }

    keyCommands.del(cacheKey(clientId));
  }

  private String cacheKey(String clientId) {
    return cacheKeyPrefix + clientId;
  }

}
