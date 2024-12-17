package it.pagopa.oneid.common.model.exception.enums;

import lombok.Getter;

@Getter
public enum FeErrorCode {
  ERRORCODE_NR08("08"),
  ERRORCODE_NR09("09"),
  ERRORCODE_NR11("11"),
  ERRORCODE_NR12("12"),
  ERRORCODE_NR13("13"),
  ERRORCODE_NR14("14"),
  ERRORCODE_NR15("15"),
  ERRORCODE_NR16("16"),
  ERRORCODE_NR17("17"),
  ERRORCODE_NR18("18"),
  ERRORCODE_NR19("19"),
  ERRORCODE_NR20("20"),
  ERRORCODE_NR21("21"),
  ERRORCODE_NR22("22"),
  ERRORCODE_NR23("23"),
  ERRORCODE_NR25("25"),
  ERRORCODE_NR30("30"),
  FE_IDP_ERROR("IDP_ERROR"),
  FE_OI_ERROR("OI_ERROR");

  private final String feErrorCode;

  FeErrorCode(String feErrorCode) {
    this.feErrorCode = feErrorCode;
  }
}
