package it.pagopa.oneid.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CieEntityIdRegistry {

  private final Set<String> entityIds;

  @Inject
  public CieEntityIdRegistry(@ConfigProperty(name = "cie_entity_ids") List<String> entityIds) {
    Set<String> normalizedEntityIds = new HashSet<>();
    for (String entityId : entityIds) {
      if (entityId == null || entityId.isBlank()) {
        continue;
      }
      normalizedEntityIds.add(entityId.trim().toLowerCase(Locale.ROOT));
    }
    this.entityIds = Set.copyOf(normalizedEntityIds);
  }

  public boolean contains(String entityId) {
    return entityId != null && entityIds.contains(entityId.trim().toLowerCase(Locale.ROOT));
  }
}
