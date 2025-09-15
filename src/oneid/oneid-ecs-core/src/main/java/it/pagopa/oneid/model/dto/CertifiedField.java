package it.pagopa.oneid.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CertifiedField<T> {

  private static final String CERTIFIED_FIELD_VALUE = "SPID";
  
  private String certification;
  private T value;

  public CertifiedField(T value) {
    this.certification = CERTIFIED_FIELD_VALUE;
    this.value = value;
  }
}
