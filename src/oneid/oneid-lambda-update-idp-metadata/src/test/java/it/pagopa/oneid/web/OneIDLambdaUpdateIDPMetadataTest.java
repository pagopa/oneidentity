package it.pagopa.oneid.web;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;

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
        ObjectMapper eventMapper = new ObjectMapper();
    Mockito.when(idpMetadataService.getMetadataFile(Mockito.any())).thenReturn("dummy");

    ArrayList<IDP> idps = new ArrayList<>(List.of(new IDP()));
    Mockito.when(idpMetadataService.parseIDPMetadata(Mockito.any(), Mockito.any()))
        .thenReturn(idps);
    JsonNode input = s3EventInput("spid-11111.xml");
    OneIDLambdaUpdateIDPMetadata handler = new OneIDLambdaUpdateIDPMetadata();
    handler.idpMetadataServiceImpl = idpMetadataService;
    handler.objectMapper = eventMapper;

    assertEquals("Ok", handler.handleRequest(input, null));

    verify(idpMetadataService).getMetadataFile("spid-11111.xml");
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

        ArgumentCaptor<JsonNode> recordCaptor = ArgumentCaptor.forClass(JsonNode.class);
        verify(idpMetadataService).isPublicIdpsStatusChange(recordCaptor.capture());
    verify(idpMetadataService).refreshPublicIdps();
        assertEquals("MODIFY", recordCaptor.getValue().path("eventName").asText());
        assertEquals(LatestTAG.LATEST_SPID.toString(),
            recordCaptor.getValue().path("dynamodb").path("NewImage").path("pointer")
                .path("S").asText());
  }

    private JsonNode s3EventInput(String objectKey) throws Exception {
        return new ObjectMapper().readTree("""
                {
                    "Records": [
                        {
                            "eventSource": "aws:s3",
                            "s3": {
                                "object": {
                                    "key": "%s"
                                }
                            }
                        }
                    ]
                }
                """.formatted(objectKey));
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
                                "ApproximateCreationDateTime": 1720944000.123,
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
