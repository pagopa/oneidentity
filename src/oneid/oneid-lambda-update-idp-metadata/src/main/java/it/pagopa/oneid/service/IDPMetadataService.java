package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.model.MetadataType;
import java.util.ArrayList;

public interface IDPMetadataService {

  ArrayList<IDP> parseIDPMetadata(String idpMetadata,
      long timestamp, LatestTAG latestTAG);

  void updateIDPMetadata(ArrayList<IDP> idpMetadata, long updatedTimestamp,
      MetadataType metadataType);

  String getMetadataFile(String fileName);

}
