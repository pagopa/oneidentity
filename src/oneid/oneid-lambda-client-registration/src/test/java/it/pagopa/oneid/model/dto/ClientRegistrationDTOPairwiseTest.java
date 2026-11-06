package it.pagopa.oneid.model.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.oneid.common.model.enums.PairwiseMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClientRegistrationDTOPairwiseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @ParameterizedTest
  @EnumSource(PairwiseMode.class)
  @DisplayName("Registration accepts and serializes pairwise enum names")
  void given_enum_when_registration_is_deserialized_then_mode_is_preserved(PairwiseMode mode)
      throws JsonProcessingException {
    ClientRegistrationDTO dto = objectMapper.readValue(
        "{\"pairwise\":\"" + mode.name() + "\"}", ClientRegistrationDTO.class);

    assertEquals(mode, dto.getPairwise());
    assertEquals(mode.name(),
        objectMapper.readTree(objectMapper.writeValueAsString(dto)).get("pairwise").asText());
  }

  @ParameterizedTest
  @ValueSource(strings = {"{}", "{\"pairwise\":null}"})
  @DisplayName("Null or omitted pairwise disables the feature")
  void given_no_mode_when_registration_is_deserialized_then_pairwise_is_disabled(String json)
      throws JsonProcessingException {
    assertNull(objectMapper.readValue(json, ClientRegistrationDTO.class).getPairwise());
  }

  @ParameterizedTest
  @ValueSource(strings = {"true", "false", "\"true\"", "\"false\"", "0", "1",
      "\"TOKEN_UNKNOWN\"", "\"token\"", "\"pdv\"", "\"\"", "\" \""})
  @DisplayName("Registration rejects boolean and non-enum pairwise values")
  void given_non_enum_when_registration_is_deserialized_then_input_is_rejected(String value) {
    assertThrows(JsonProcessingException.class,
        () -> objectMapper.readValue("{\"pairwise\":" + value + "}", ClientRegistrationDTO.class));
  }
}
