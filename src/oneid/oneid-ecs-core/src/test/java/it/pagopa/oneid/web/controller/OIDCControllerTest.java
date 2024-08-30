package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.model.dto.JWKSSetDTO;
import it.pagopa.oneid.service.OIDCServiceImpl;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class OIDCControllerTest {


  @InjectMock
  private OIDCServiceImpl oidcServiceImpl;
  @Inject
  private OIDCController oidcController;


  @Test
  void keys() {
    JWKSSetDTO jwksPublicKey = Mockito.mock(JWKSSetDTO.class);
    Mockito.when(oidcServiceImpl.getJWKSPublicKey()).thenReturn(jwksPublicKey);

    String response =
        given()
            .when().get("/oidc/keys")
            .then()
            .statusCode(200)
            .extract()
            .asString();
    assertNotNull(response);
  }

  //TODO remember to use alternative method on session because it's scoped
  
  @Test
  void authorizePost() {
  }

  @Test
  void authorizeGet() {
  }

  @Test
  void token() {
  }
}