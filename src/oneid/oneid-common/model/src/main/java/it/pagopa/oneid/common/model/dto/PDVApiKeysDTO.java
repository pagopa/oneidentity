package it.pagopa.oneid.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@RegisterForReflection
@SuperBuilder
public class PDVApiKeysDTO {
    @JsonProperty("api_keys")
    private List<PDVPlanDTO> apiKeys;
}