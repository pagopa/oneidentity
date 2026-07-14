package it.pagopa.oneid.web;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3ObjectEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.service.IDPMetadataServiceImpl;

class OneIDLambdaUpdateIDPMetadataTest {

  @Test
  @DisplayName("given an S3 event when handling then update metadata and publish public IDPs")
  void given_s3Event_when_handling_then_updatesMetadataAndPublishesPublicIdps() throws Exception {
    IDPMetadataServiceImpl idpMetadataService = Mockito.mock(IDPMetadataServiceImpl.class);
    ObjectMapper eventMapper = Mockito.mock(ObjectMapper.class);
    S3Event s3Event = Mockito.mock(S3Event.class);
    S3EventNotificationRecord s3EventNotificationRecord = Mockito.mock(
        S3EventNotificationRecord.class);
    S3Entity s3Entity = Mockito.mock(S3Entity.class);
    S3ObjectEntity s3ObjectEntity = Mockito.mock(S3ObjectEntity.class);
    Mockito.when(s3ObjectEntity.getUrlDecodedKey()).thenReturn("spid-11111.xml");
    Mockito.when(s3Entity.getObject()).thenReturn(s3ObjectEntity);
    Mockito.when(s3EventNotificationRecord.getS3()).thenReturn(s3Entity);
    Mockito.when(s3Event.getRecords()).thenReturn(List.of(s3EventNotificationRecord));
    Mockito.when(idpMetadataService.getMetadataFile(Mockito.any())).thenReturn("dummy");

    ArrayList<IDP> idps = new ArrayList<>(List.of(new IDP()));
    Mockito.when(idpMetadataService.parseIDPMetadata(Mockito.any(), Mockito.any()))
        .thenReturn(idps);
    JsonNode input = eventInput("aws:s3");
    Mockito.when(eventMapper.convertValue(input, S3Event.class)).thenReturn(s3Event);
    OneIDLambdaUpdateIDPMetadata handler = new OneIDLambdaUpdateIDPMetadata();
    handler.idpMetadataServiceImpl = idpMetadataService;
    handler.objectMapper = eventMapper;

    assertEquals("Ok", handler.handleRequest(input, null));

    verify(idpMetadataService).updateIDPMetadata(Mockito.same(idps), Mockito.any(
        IdpS3FileDTO.class));
    verify(idpMetadataService).publishPublicIdps(Mockito.same(idps), Mockito.any(
        IdpS3FileDTO.class));
  }

  @Test
    @DisplayName("given a DynamoDB status change when handling then validate and refresh public IDPs")
    void given_dynamodbStatusChange_when_handling_then_validatesAndRefreshesPublicIdps()
      throws Exception {
    IDPMetadataServiceImpl idpMetadataService = Mockito.mock(IDPMetadataServiceImpl.class);
        ObjectMapper eventMapper = new ObjectMapper();
        JsonNode input = dynamodbEventInput("MODIFY", LatestTAG.LATEST_SPID.toString(), "OK", "KO");
        Mockito.when(idpMetadataService.isPublicIdpsStatusChange(Mockito.any())).thenReturn(true);
    OneIDLambdaUpdateIDPMetadata handler = new OneIDLambdaUpdateIDPMetadata();
    handler.idpMetadataServiceImpl = idpMetadataService;
    handler.objectMapper = eventMapper;

    assertEquals("Ok", handler.handleRequest(input, null));

        ArgumentCaptor<DynamodbEvent.DynamodbStreamRecord> recordCaptor = ArgumentCaptor.forClass(
            DynamodbEvent.DynamodbStreamRecord.class);
        verify(idpMetadataService).validateDynamodbStatus(recordCaptor.capture());
        verify(idpMetadataService).isPublicIdpsStatusChange(recordCaptor.getValue());
    verify(idpMetadataService).refreshPublicIdps();
        assertEquals("MODIFY", recordCaptor.getValue().getEventName());
        assertEquals(LatestTAG.LATEST_SPID.toString(),
            recordCaptor.getValue().getDynamodb().getNewImage().get("pointer").getS());
  }

  private JsonNode eventInput(String eventSource) throws Exception {
    return new ObjectMapper().readTree("""
        { "Records": [{ "eventSource": "%s" }] }
        """.formatted(eventSource));
  }

    private JsonNode dynamodbEventInput(String eventName, String pointer, String oldStatus,
            String newStatus) throws Exception {
        return new ObjectMapper().readTree("""
                {
                    "Records": [
                        {
                            "eventName": "%s",
                            "eventSource": "aws:dynamodb",
                            "dynamodb": {
                                "OldImage": {
                                    "pointer": { "S": "%s" },
                                    "status": { "S": "%s" }
                                },
                                "NewImage": {
                                    "pointer": { "S": "%s" },
                                    "status": { "S": "%s" }
                                }
                            }
                        }
                    ]
                }
                """.formatted(eventName, pointer, oldStatus, pointer, newStatus));
    }

}
