package it.pagopa.oneid.service;

import io.quarkus.runtime.Startup;
import it.pagopa.oneid.model.session.SAMLSession;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Startup
public class BrowserBindingService {

  public enum Mode {
    OFF, MONITOR, ENFORCE
  }

  public enum Outcome {
    MATCHED, MISSING, MISMATCH, EXPIRED, LEGACY
  }

  private static final long COOKIE_AGE_SECONDS = 900;
  private static final SecureRandom RANDOM = new SecureRandom();

  @ConfigProperty(name = "browser_binding_mode", defaultValue = "OFF")
  Mode mode;

  @ConfigProperty(name = "browser_binding_legacy_cutoff", defaultValue = "0")
  long legacyCutoff;

  @Inject
  Clock clock;

  @PostConstruct
  void validateConfiguration() {
    if (mode == Mode.ENFORCE
        && (legacyCutoff <= 0 || legacyCutoff > clock.instant().getEpochSecond())) {
      throw new IllegalStateException("Browser binding enforcement requires a legacy cutoff");
    }
  }

  public boolean enabled() {
    return mode != Mode.OFF;
  }

  public boolean enforcing() {
    return mode == Mode.ENFORCE;
  }

  public Mode mode() {
    return mode;
  }

  public String issue(SAMLSession session) {
    byte[] secret = new byte[32];
    RANDOM.nextBytes(secret);
    String value = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    session.setBrowserBindingDigest(digest(value));
    session.setBrowserBindingExpiresAt(clock.instant().getEpochSecond() + COOKIE_AGE_SECONDS);
    // Temporary alarm test: return a cookie that cannot match the stored digest.
    return name(session.getSamlRequestID()) + "=" + value + "-mismatch"
        + "; Max-Age=" + COOKIE_AGE_SECONDS + "; Path=/; Secure; HttpOnly; SameSite=None";
  }

  public Outcome verify(SAMLSession session, Map<String, Cookie> cookies) {
    long now = clock.instant().getEpochSecond();
    if (session.getTtl() <= now) {
      return Outcome.EXPIRED;
    }
    if (session.getBrowserBindingDigest() == null || session.getBrowserBindingExpiresAt() == null) {
      return legacyCutoff > 0 && session.getCreationTime() < legacyCutoff
          ? Outcome.LEGACY
          : Outcome.MISSING;
    }
    if (now >= session.getBrowserBindingExpiresAt()) {
      return Outcome.EXPIRED;
    }
    Cookie cookie = cookies.get(name(session.getSamlRequestID()));
    if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
      return Outcome.MISSING;
    }
    byte[] expected = session.getBrowserBindingDigest().getBytes(StandardCharsets.US_ASCII);
    byte[] actual = digest(cookie.getValue()).getBytes(StandardCharsets.US_ASCII);
    return MessageDigest.isEqual(expected, actual) ? Outcome.MATCHED : Outcome.MISMATCH;
  }

  public String clear(SAMLSession session) {
    return name(session.getSamlRequestID()) + "=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=None";
  }

  private String name(String requestId) {
    return "__Host-OI-" + digest(requestId);
  }

  private String digest(String value) {
    try {
      return Base64.getUrlEncoder().withoutPadding().encodeToString(
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
