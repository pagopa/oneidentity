package it.pagopa.oneid.common.model.exception.enums;

import lombok.Getter;

@Getter
public enum FeErrorCode {
  ERRORCODE_NR19("19"),
  ERRORCODE_NR20("20"),
  ERRORCODE_NR21("21"),
  ERRORCODE_NR22("22"),
  ERRORCODE_NR23("23"),
  ERRORCODE_NR25("25"),
  FE_IDP_ERROR("IDP_ERROR"),
  FE_OI_ERROR("OI_ERROR");

  private final String feErrorCode;

  FeErrorCode(String feErrorCode) {
    this.feErrorCode = feErrorCode;
  }
}
