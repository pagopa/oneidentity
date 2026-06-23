package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import it.pagopa.oneid.exception.InvalidAccessTokenException;
import it.pagopa.oneid.web.dto.UserInfoResponseDTO;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserInfoServiceImplTest {

  @Test
  void buildUserInfoResponse_preservesClaimsFromIdToken() throws Exception {
    UserInfoServiceImpl userInfoService = new UserInfoServiceImpl();
    Method buildUserInfoResponse = UserInfoServiceImpl.class.getDeclaredMethod(
        "buildUserInfoResponse", String.class);
    buildUserInfoResponse.setAccessible(true);

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject("subject-123")
        .issuer("https://issuer.example.com")
        .audience("client-id")
        .issueTime(new Date())
        .expirationTime(new Date(System.currentTimeMillis() + 60000))
        .claim("nonce", "nonce-value")
        .claim("sameIdp", true)
        .claim("fiscalNumber", "ABCDEF12G34H567I")
        .claim("pairwise", "pairwise-value")
        .build();

    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
        claimsSet);
    String serializedSignedJwt = signedJWT.getHeader().toBase64URL()
      + "."
      + signedJWT.getPayload().toBase64URL()
      + ".c2lnbmF0dXJl";

    UserInfoResponseDTO response = (UserInfoResponseDTO) buildUserInfoResponse.invoke(
        userInfoService,
      serializedSignedJwt);

    assertTrue(response.getClaims().containsKey("sub"));
    assertTrue(response.getClaims().containsKey("fiscalNumber"));
    assertTrue(response.getClaims().containsKey("pairwise"));
    assertEquals(List.of("client-id"), response.getClaims().get("aud"));
    assertEquals("https://issuer.example.com", response.getClaims().get("iss"));
    assertEquals("nonce-value", response.getClaims().get("nonce"));
    assertEquals(true, response.getClaims().get("sameIdp"));
  }

  @Test
  void buildUserInfoResponse_invalidJwtThrowsInvalidAccessTokenException() throws Exception {
    UserInfoServiceImpl userInfoService = new UserInfoServiceImpl();
    Method buildUserInfoResponse = UserInfoServiceImpl.class.getDeclaredMethod(
        "buildUserInfoResponse", String.class);
    buildUserInfoResponse.setAccessible(true);

    InvocationTargetException exception = assertThrows(InvocationTargetException.class,
        () -> buildUserInfoResponse.invoke(userInfoService, "not-a-jwt"));

    assertInstanceOf(InvalidAccessTokenException.class, exception.getCause());
    assertEquals("invalid_token", exception.getCause().getMessage());
  }
}
