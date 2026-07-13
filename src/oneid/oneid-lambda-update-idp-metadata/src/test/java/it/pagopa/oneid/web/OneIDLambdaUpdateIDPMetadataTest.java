package it.pagopa.oneid.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
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
    ObjectMapper eventMapper = Mockito.mock(ObjectMapper.class);
    JsonNode input = eventInput("aws:dynamodb");
        DynamodbEvent dynamodbEvent = dynamodbEvent("MODIFY", LatestTAG.LATEST_SPID.toString(),
                "OK", "KO");
        DynamodbEvent.DynamodbStreamRecord record = dynamodbEvent.getRecords().getFirst();
    Mockito.when(eventMapper.convertValue(input, DynamodbEvent.class)).thenReturn(dynamodbEvent);
        Mockito.when(idpMetadataService.isPublicIdpsStatusChange(record)).thenReturn(true);
    OneIDLambdaUpdateIDPMetadata handler = new OneIDLambdaUpdateIDPMetadata();
    handler.idpMetadataServiceImpl = idpMetadataService;
    handler.objectMapper = eventMapper;

    assertEquals("Ok", handler.handleRequest(input, null));

    verify(idpMetadataService).validateDynamodbStatus(record);
    verify(idpMetadataService).isPublicIdpsStatusChange(record);
    verify(idpMetadataService).refreshPublicIdps();
  }

  private JsonNode eventInput(String eventSource) throws Exception {
    return new ObjectMapper().readTree("""
        { "Records": [{ "eventSource": "%s" }] }
        """.formatted(eventSource));
  }

    private DynamodbEvent dynamodbEvent(String eventName, String pointer, String oldStatus,
            String newStatus) {
        StreamRecord dynamodb = new StreamRecord();
        dynamodb.setOldImage(Map.of(
                "pointer", stringAttribute(pointer),
                "status", stringAttribute(oldStatus)));
        dynamodb.setNewImage(Map.of(
                "pointer", stringAttribute(pointer),
                "status", stringAttribute(newStatus)));

        DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
        record.setEventName(eventName);
        record.setDynamodb(dynamodb);

        DynamodbEvent event = new DynamodbEvent();
        event.setRecords(List.of(record));
        return event;
    }

    private AttributeValue stringAttribute(String value) {
        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setS(value);
        return attributeValue;
    }

}
