package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
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
  BigInteger module;

  @JsonProperty("e")
  BigInteger exponent;

  public JWKSUriMetadataDTO(String keyID, String usage, RSAPublicKey rsaPublicKey) {
    this.keyType = "RSA";
    this.keyID = keyID;
    this.usage = usage;
    this.algorithm = rsaPublicKey.getAlgorithm();
    this.module = rsaPublicKey.getModulus();
    this.exponent = rsaPublicKey.getPublicExponent();
  }

}
