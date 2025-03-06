package it.pagopa.oneid.web;

import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@ApplicationScoped
public class SPARouting {
  // Currently, for technical reasons, the Quinoa SPA routing configuration won’t
  // work with RESTEasy Classic. Instead, you may use a workaround (if your app
  // has all the rest resources under the same path prefix):
  // https://docs.quarkiverse.io/quarkus-quinoa/dev/advanced-guides.html

  // Vite in dev mode requests /@vite/client and /@reactrefresh so add "/@" if you
  // use Vite

  // we might think to get them from properties file
  private static final String[] PATH_PREFIXES = {"/oidc/", "/api/", "/@",
      "/saml/",
      "/.well-known/", "/clients", "/idps"};
  private static final Predicate<String> FILE_NAME_PREDICATE = Pattern.compile(".+\\.[a-zA-Z0-9]+$")
      .asMatchPredicate();

  public void init(@Observes Router router) {
    router.get("/*").handler(rc -> {
      final String path = rc.normalizedPath();
      if (!path.equals("/")
          && Stream.of(PATH_PREFIXES).noneMatch(path::startsWith)
          && !FILE_NAME_PREDICATE.test(path)) {
        rc.reroute("/");
      } else {
        rc.next();
      }
    });
  }
}
