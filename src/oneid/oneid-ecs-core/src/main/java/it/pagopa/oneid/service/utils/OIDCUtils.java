package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.connector.KMSConnector.concatenateArrays;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import it.pagopa.oneid.connector.KMSConnectorImpl;
import it.pagopa.oneid.model.dto.AttributeDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import software.amazon.awssdk.services.kms.model.SignResponse;

@ApplicationScoped
public class OIDCUtils {

  private static final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();

  @Inject
  KMSConnectorImpl kmsConnectorImpl;

  private static JWTClaimsSet buildJWTClaimsSet(List<AttributeDTO> attributeDTOList, String nonce) {
    return new JWTClaimsSet.Builder()
        .subject("one-id")
        .issuer("http://dev.oneidentity.pagopa.it/token")
        .expirationTime(new Date(new Date().getTime() + 60 * 1000))
        .claim("fiscalNumber", fiscalNumber)
        .claim("nonce", nonce)
        .build();
  }

  public String createSignedJWT(List<AttributeDTO> attributeDTOList, String nonce) {
    // Prepare header for JWT
    JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
        .type(JOSEObjectType.JWT)
        .build();
    byte[] headerBytes = jwsHeader.toString().getBytes();
    byte[] encodedHeader = base64UrlEncoder.encodeToString(headerBytes).getBytes();

    // Prepare claims set for JWT

    byte[] payloadBytes = buildJWTClaimsSet(attributeDTOList, nonce).toPayload().toBytes();
    byte[] encodedPayload = base64UrlEncoder.encodeToString(payloadBytes).getBytes();

    // Create a signature with base64 encoded header and payload
    SignResponse signResponse = kmsConnectorImpl.sign(encodedHeader, encodedPayload);
    String base64Sign = base64UrlEncoder.encodeToString(signResponse.signature().asByteArray());

    // Concatenation of JWT parts to obtain the pattern header.payload.signature
    byte[] headerPayloadBytes = concatenateArrays(encodedHeader, (byte) '.', encodedPayload);
    return new String(concatenateArrays(headerPayloadBytes, (byte) '.', base64Sign.getBytes()));
  }


}
