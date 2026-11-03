package it.pagopa.oneid.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CieEntityIdRegistry {

    private final Set<String> entityIds;

    @Inject
    public CieEntityIdRegistry(
            @ConfigProperty(name = "cie_entity_ids") Optional<String> configuredEntityIds,
            @ConfigProperty(name = "cie_entity_id") Optional<String> legacyEntityId,
            @ConfigProperty(name = "cie_additional_entity_ids") Optional<String> legacyAdditionalEntityIds) {
        this(configuredEntityIds(configuredEntityIds, legacyEntityId, legacyAdditionalEntityIds));
    }

    public CieEntityIdRegistry(List<String> entityIds) {
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

    private static List<String> configuredEntityIds(Optional<String> configuredEntityIds,
            Optional<String> legacyEntityId, Optional<String> legacyAdditionalEntityIds) {
        if (configuredEntityIds.isPresent() && !configuredEntityIds.get().isBlank()) {
            List<String> entityIds = split(configuredEntityIds.get());
            if (!entityIds.isEmpty()) {
                return entityIds;
            }
        }

        List<String> legacyIds = new ArrayList<>();
        legacyEntityId.ifPresent(legacyIds::add);
        legacyAdditionalEntityIds.ifPresent(additionalIds -> legacyIds.addAll(split(additionalIds)));
        return List.copyOf(legacyIds);
    }

    private static List<String> split(String entityIds) {
        List<String> parsedEntityIds = new ArrayList<>();
        for (String entityId : entityIds.split(",", -1)) {
            if (entityId != null && !entityId.isBlank()) {
                parsedEntityIds.add(entityId.trim());
            }
        }
        return List.copyOf(parsedEntityIds);
    }
}
