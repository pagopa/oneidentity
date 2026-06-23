package it.pagopa.oneid.web.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class UserInfoResponseDTOTest {

  @Test
  void serializeUserInfoResponse_includesOnlyPairwiseClaimFromPayload()
      throws JsonProcessingException {
    UserInfoResponseDTO userInfoResponseDTO = new UserInfoResponseDTO();
    userInfoResponseDTO.addClaim("sub", "subject-123");
    userInfoResponseDTO.setPairwiseClaim("pairwise-value");

    String serialized = new ObjectMapper().writeValueAsString(userInfoResponseDTO);

    assertTrue(serialized.contains("\"pairwise\":\"pairwise-value\""));
    assertFalse(serialized.contains("pairwiseClaim"));
  }
}
