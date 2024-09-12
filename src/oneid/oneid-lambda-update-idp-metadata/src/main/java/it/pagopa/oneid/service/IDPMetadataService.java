package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import java.util.ArrayList;

public interface IDPMetadataService {

  ArrayList<IDP> parseIDPMetadata(String idpMetadata, IdpS3FileDTO idpS3FileDTO);

  void updateIDPMetadata(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO);

  String getMetadataFile(String fileName);

}
