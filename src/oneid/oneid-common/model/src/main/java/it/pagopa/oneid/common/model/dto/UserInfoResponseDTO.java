package it.pagopa.oneid.common.model.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponseDTO {

  public static final String PAIRWISE_CLAIM = "pairwise";

  @Builder.Default
  private final Map<String, Object> claims = new LinkedHashMap<>();

  @JsonAnyGetter
  public Map<String, Object> getClaims() {
    return claims;
  }

  @JsonAnySetter
  public void addClaim(String name, Object value) {
    claims.put(name, value);
  }

  public boolean hasPairwiseClaim() {
    return getPairwiseClaim().isPresent();
  }

  public Optional<String> getPairwiseClaim() {
    Object pairwise = claims.get(PAIRWISE_CLAIM);
    if (!(pairwise instanceof String pairwiseValue) || pairwiseValue.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(pairwiseValue);
  }

  public void setPairwiseClaim(String pairwise) {
    claims.put(PAIRWISE_CLAIM, pairwise);
  }
}
