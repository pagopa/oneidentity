package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CieEntityIdRegistryTest {

    private static final String PREPRODUCTION = "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO";
    private static final String COLLAUDO = "https://collaudo.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO";

    @Test
    @DisplayName("The canonical list takes precedence and matches normalized entity IDs")
    void given_canonical_list_when_matching_entity_then_use_canonical_values() {
        CieEntityIdRegistry registry = new CieEntityIdRegistry(
                Optional.of(" " + PREPRODUCTION + " , " + COLLAUDO + " "),
                Optional.of("https://legacy.example/idp"),
                Optional.empty());

        assertTrue(registry.contains(PREPRODUCTION.toUpperCase()));
        assertTrue(registry.contains(COLLAUDO));
        assertFalse(registry.contains("https://legacy.example/idp"));
        assertFalse(registry.contains("https://unknown.example/idp"));
        assertFalse(registry.contains(null));
    }

    @Test
    @DisplayName("Legacy primary and additional entity IDs are combined when the canonical list is absent")
    void given_legacy_configuration_when_matching_entity_then_use_combined_values() {
        CieEntityIdRegistry registry = new CieEntityIdRegistry(
                Optional.empty(), Optional.of(PREPRODUCTION), Optional.of(" , " + COLLAUDO + " , "));

        assertTrue(registry.contains(PREPRODUCTION));
        assertTrue(registry.contains(COLLAUDO));
        assertFalse(registry.contains("https://unknown.example/idp"));
    }

    @Test
    @DisplayName("The explicit entity list is normalized and duplicate IDs are collapsed")
    void given_entity_list_when_creating_registry_then_normalize_and_deduplicate() {
        CieEntityIdRegistry registry = new CieEntityIdRegistry(
                List.of(" " + PREPRODUCTION + " ", PREPRODUCTION.toUpperCase()));

        assertTrue(registry.contains(PREPRODUCTION));
        assertTrue(registry.contains(" " + PREPRODUCTION.toUpperCase() + " "));
        assertFalse(registry.contains(COLLAUDO));
    }
}
