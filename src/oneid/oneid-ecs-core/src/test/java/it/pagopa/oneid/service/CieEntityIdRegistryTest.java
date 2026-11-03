package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CieEntityIdRegistryTest {

    private static final String PREPRODUCTION = "https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO";
    private static final String COLLAUDO = "https://collaudo.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO";

    @Test
    @DisplayName("The explicit entity list is normalized and duplicate IDs are collapsed")
    void given_entity_list_when_creating_registry_then_normalize_and_deduplicate() {
        CieEntityIdRegistry registry = new CieEntityIdRegistry(
                List.of(" " + PREPRODUCTION + " ", PREPRODUCTION.toUpperCase(), COLLAUDO));

        assertTrue(registry.contains(PREPRODUCTION));
        assertTrue(registry.contains(" " + PREPRODUCTION.toUpperCase() + " "));
        assertTrue(registry.contains(COLLAUDO));
        assertFalse(registry.contains("https://unknown.example/idp"));
        assertFalse(registry.contains(null));
    }

    @Test
    @DisplayName("An empty entity list does not classify any provider as CIE")
    void given_empty_entity_list_when_matching_entity_then_return_false() {
        CieEntityIdRegistry registry = new CieEntityIdRegistry(List.of());

        assertFalse(registry.contains(PREPRODUCTION));
    }
}
