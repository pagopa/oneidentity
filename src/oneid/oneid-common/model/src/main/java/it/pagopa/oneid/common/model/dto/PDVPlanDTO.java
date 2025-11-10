package it.pagopa.oneid.common.model.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@RegisterForReflection
@SuperBuilder
public class PDVPlanDTO {

  private String id;

  private String name;
}
