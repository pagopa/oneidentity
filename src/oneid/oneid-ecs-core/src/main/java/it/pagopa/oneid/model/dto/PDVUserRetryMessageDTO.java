package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.oneid.common.model.dto.SavePDVUserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PDVUserRetryMessageDTO {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL);

  private String clientId;
  private SavePDVUserDTO savePDVUserDTO;

  public String toJson() {
    try {
      return OBJECT_MAPPER.writeValueAsString(this);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize PDVUserRetryMessageDTO to JSON", e);
    }
  }

}
