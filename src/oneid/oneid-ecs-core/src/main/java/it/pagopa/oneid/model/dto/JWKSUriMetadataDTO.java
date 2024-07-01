package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.JWSAlgorithm;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class JWKSUriMetadataDTO {

  @JsonProperty("kty")
  String keyType;

  @JsonProperty("kid")
  String keyID;

  @JsonProperty("use")
  String usage;

  @JsonProperty("alg")
  String algorithm;

  @JsonProperty("n")
  String module;

  @JsonProperty("e")
  String exponent;

  public JWKSUriMetadataDTO(String keyID, RSAPublicKey rsaPublicKey) {
    this.keyType = rsaPublicKey.getAlgorithm();
    this.keyID = keyID;
    this.usage = "sig";
    this.algorithm = JWSAlgorithm.RS256.getName();
    this.module = Base64.getUrlEncoder().encodeToString(rsaPublicKey.getModulus().toByteArray());
    this.exponent = Base64.getUrlEncoder()
        .encodeToString(rsaPublicKey.getPublicExponent().toByteArray());
  }

}
