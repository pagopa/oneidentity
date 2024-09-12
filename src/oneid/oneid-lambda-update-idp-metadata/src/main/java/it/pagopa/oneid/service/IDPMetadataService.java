package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.model.enums.MetadataType;
import java.util.ArrayList;

public interface IDPMetadataService {

  ArrayList<IDP> parseIDPMetadata(String idpMetadata,
      long timestamp, LatestTAG latestTAG);

  ArrayList<IDP> parseIDPMetadata(String idpMetadata, IdpS3FileDTO idpS3FileDTO);

  void updateIDPMetadata(ArrayList<IDP> idpMetadata, long updatedTimestamp,
      MetadataType metadataType);

  void updateIDPMetadata(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO);

  String getMetadataFile(String fileName);

}
