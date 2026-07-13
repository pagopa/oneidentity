package it.pagopa.oneid.service;

import java.util.ArrayList;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;

public interface IDPMetadataService {

  ArrayList<IDP> parseIDPMetadata(String idpMetadata, IdpS3FileDTO idpS3FileDTO);

  void updateIDPMetadata(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO);

  void publishPublicIdps(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO);

  void validateDynamodbStatus(DynamodbEvent.DynamodbStreamRecord record);

  boolean isPublicIdpsStatusChange(DynamodbEvent.DynamodbStreamRecord record);

  void refreshPublicIdps();

  String getMetadataFile(String fileName);

}
