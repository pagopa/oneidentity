package it.pagopa.oneid.service;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;

public interface IDPMetadataService {

  ArrayList<IDP> parseIDPMetadata(String idpMetadata, IdpS3FileDTO idpS3FileDTO);

  void updateIDPMetadata(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO);

  void publishPublicIdps(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO);

  boolean isPublicIdpsStatusChange(JsonNode record);

  void refreshPublicIdps();

  String getMetadataFile(String fileName);

}
