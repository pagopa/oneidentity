package it.pagopa.oneid.service.utils;

import static it.pagopa.oneid.common.utils.SAMLUtilsConstants.BASE_PATH;
import static it.pagopa.oneid.connector.KMSConnector.concatenateArrays;
import static it.pagopa.oneid.connector.utils.ConnectorConstants.VALID_TIME_JWT_MIN;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.kms.model.SignResponse;

@ApplicationScoped
public class OIDCUtils {

  private static final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();

  @Inject
  @ConfigProperty(name = "kms_key_id")
  String KMS_KEY_ID;

  @Inject
  KMSConnectorImpl kmsConnectorImpl;

  private static JWTClaimsSet buildJWTClaimsSet(String requestId, String clientId,
      List<AttributeDTO> attributeDTOList, String nonce) {
    JWTClaimsSet.Builder jwtClaimsSet = new JWTClaimsSet.Builder()
        .subject(requestId)
        .issuer(BASE_PATH)
        .audience(clientId)
        .issueTime(new Date())
        .claim("nonce", nonce)
        .expirationTime(new Date(new Date().getTime() + (long) VALID_TIME_JWT_MIN * 60 * 1000));

    attributeDTOList.forEach(attributeDTO -> jwtClaimsSet.claim(attributeDTO.getAttributeName(),
        attributeDTO.getAttributeValue()));

    return jwtClaimsSet.build();
  }

  public String createSignedJWT(String requestId, String clientId,
      List<AttributeDTO> attributeDTOList,
      String nonce) {
    // Prepare header for JWT
    JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
        .type(JOSEObjectType.JWT)
        .build();
    byte[] headerBytes = jwsHeader.toString().getBytes();
    byte[] encodedHeader = base64UrlEncoder.encodeToString(headerBytes).getBytes();

    // Prepare claims set for JWT

    byte[] payloadBytes = buildJWTClaimsSet(requestId, clientId, attributeDTOList,
        nonce).toPayload()
        .toBytes();
    byte[] encodedPayload = base64UrlEncoder.encodeToString(payloadBytes).getBytes();

    // Create a signature with base64 encoded header and payload
    SignResponse signResponse = kmsConnectorImpl.sign(KMS_KEY_ID, encodedHeader, encodedPayload);
    String base64Sign = base64UrlEncoder.encodeToString(signResponse.signature().asByteArray());

    // Concatenation of JWT parts to obtain the pattern header.payload.signature
    byte[] headerPayloadBytes = concatenateArrays(encodedHeader, (byte) '.', encodedPayload);
    return new String(concatenateArrays(headerPayloadBytes, (byte) '.', base64Sign.getBytes()));
  }

}
