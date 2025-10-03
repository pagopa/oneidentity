package it.pagopa.oneid.common.model.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@SuperBuilder
public class PDVPlanDTO {

  private String id;

  private String name;
}
