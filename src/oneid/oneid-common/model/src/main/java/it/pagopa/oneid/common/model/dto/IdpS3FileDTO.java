package it.pagopa.oneid.common.model.dto;

import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.model.enums.MetadataType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdpS3FileDTO {

  MetadataType metadataType;

  long timestamp;

  LatestTAG latestTAG;

  public IdpS3FileDTO(String fileName) {

    //todo can we do this better?
    String[] split = fileName.split("-");
    String keyType = split[0];
    String keyTimestamp = split[1].split("\\.")[0];

    this.metadataType = MetadataType.valueOf(keyType.toUpperCase());
    this.timestamp = Long.parseLong(keyTimestamp);
    this.latestTAG = switch (metadataType) {
      case SPID -> LatestTAG.LATEST_SPID;
      case CIE -> LatestTAG.LATEST_CIE;
      case EIDAS -> LatestTAG.LATEST_EIDAS;
    };
  }

  @Override
  public String toString() {
    return metadataType.toString().toLowerCase() + "-" + timestamp + ".xml";
  }
}
