package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import jakarta.ws.rs.core.Cookie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BrowserBindingServiceTest {

    private final BrowserBindingService service = new BrowserBindingService();
    private static final Instant NOW = Instant.parse("2026-09-24T10:00:00Z");

    @BeforeEach
    void setUp() {
        service.clock = Clock.fixed(NOW, ZoneOffset.UTC);
    }

    @Test
    @DisplayName("An issued cookie matches only the originating SAML request")
    void givenTwoRequests_whenVerifyingCookies_thenBothRemainIndependent() {
        SAMLSession first = session("first");
        SAMLSession second = session("second");
        String firstHeader = service.issue(first);
        Cookie firstCookie = cookie(firstHeader);
        Cookie secondCookie = cookie(service.issue(second));
        Map<String, Cookie> cookies = Map.of(firstCookie.getName(), firstCookie,
                secondCookie.getName(), secondCookie);

        assertNotEquals(firstCookie.getName(), secondCookie.getName());
        assertTrue(firstHeader.contains("Max-Age=900"));
        assertEquals(NOW.getEpochSecond() + 900, first.getBrowserBindingExpiresAt());
        assertEquals(BrowserBindingService.Outcome.MATCHED, service.verify(first, cookies));
        assertEquals(BrowserBindingService.Outcome.MATCHED, service.verify(second, cookies));
        assertEquals(BrowserBindingService.Outcome.MISSING,
                service.verify(first, Map.of(secondCookie.getName(), secondCookie)));
        assertEquals(BrowserBindingService.Outcome.MISMATCH, service.verify(first,
                Map.of(firstCookie.getName(), new Cookie.Builder(firstCookie.getName())
                        .value(secondCookie.getValue()).build())));
        assertFalse(first.getBrowserBindingDigest().contains(firstCookie.getValue()));
        assertTrue(service.clear(first).contains("Max-Age=0"));
    }

    @Test
    @DisplayName("An expired browser binding cannot be used")
    void givenExpiredBinding_whenVerifying_thenExpired() {
        SAMLSession session = session("expired");
        Cookie cookie = cookie(service.issue(session));
        session.setBrowserBindingExpiresAt(NOW.getEpochSecond() - 1);

        assertEquals(BrowserBindingService.Outcome.EXPIRED,
                service.verify(session, Map.of(cookie.getName(), cookie)));
    }

    @Test
    @DisplayName("Only sessions created before the cutoff are legacy")
    void givenCutoff_whenVerifyingUnboundSessions_thenLegacyOnlyBeforeCutoff() {
        service.legacyCutoff = NOW.getEpochSecond();
        SAMLSession old = session("old");
        old.setCreationTime(service.legacyCutoff - 1);
        SAMLSession recent = session("recent");
        recent.setCreationTime(service.legacyCutoff);

        assertEquals(BrowserBindingService.Outcome.LEGACY, service.verify(old, Map.of()));
        assertEquals(BrowserBindingService.Outcome.MISSING, service.verify(recent, Map.of()));
        old.setTtl(service.legacyCutoff - 1);
        assertEquals(BrowserBindingService.Outcome.EXPIRED, service.verify(old, Map.of()));
    }

    @Test
    @DisplayName("Rollout modes enable binding and enforce the cutoff when required")
    void givenRolloutMode_whenValidating_thenRequireCutoffOnlyForEnforcement() {
        service.mode = BrowserBindingService.Mode.OFF;
        assertFalse(service.enabled());
        service.validateConfiguration();
        service.mode = BrowserBindingService.Mode.MONITOR;
        assertTrue(service.enabled());
        assertFalse(service.enforcing());
        service.validateConfiguration();
        service.mode = BrowserBindingService.Mode.ENFORCE;
        assertTrue(service.enforcing());
        assertThrows(IllegalStateException.class, service::validateConfiguration);
        service.legacyCutoff = NOW.getEpochSecond() + 3600;
        assertThrows(IllegalStateException.class, service::validateConfiguration);
        service.legacyCutoff = NOW.getEpochSecond();
        service.validateConfiguration();
    }

    private SAMLSession session(String id) {
        long now = NOW.getEpochSecond();
        return new SAMLSession(id, RecordType.SAML, now, now + 172800, "request", null);
    }

    private Cookie cookie(String header) {
        String[] parts = header.substring(0, header.indexOf(';')).split("=", 2);
        return new Cookie.Builder(parts[0]).value(parts[1]).build();
    }
}
