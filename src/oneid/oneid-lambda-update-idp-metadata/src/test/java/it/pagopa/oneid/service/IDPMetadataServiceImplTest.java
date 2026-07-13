package it.pagopa.oneid.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.connector.PublicIdpsBucketConnector;
import it.pagopa.oneid.connector.S3BucketIDPMetadataConnectorImpl;
import jakarta.inject.Inject;

@QuarkusTest
public class IDPMetadataServiceImplTest {

  @InjectMock
  S3BucketIDPMetadataConnectorImpl s3BucketIDPMetadataConnector;

  @InjectMock
  IDPConnectorImpl idpConnectorImpl;

  @InjectMock
  PublicIdpsBucketConnector publicIdpsBucketConnector;

  @Inject
  IDPMetadataServiceImpl idpMetadataServiceImpl;

  @Test
  void getMetadataFile() {
    Mockito.when(s3BucketIDPMetadataConnector.getMetadataFile(Mockito.any())).thenReturn("dummy");

    // then
    assertEquals("dummy", idpMetadataServiceImpl.getMetadataFile("foobar"));
  }

  @Test
  void updateIDPMetadata() {
    Mockito.doNothing().when(idpConnectorImpl)
        .saveIDPs(Mockito.any(), Mockito.any(), Mockito.any());

    // then
    ArrayList<IDP> idpList = new ArrayList<>();
    idpList.add(new IDP());

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO("spid-11111.xml");

    // then
    assertDoesNotThrow(() -> idpMetadataServiceImpl.updateIDPMetadata(idpList, idpS3FileDTO));
  }

  @Test
  void refreshPublicIdps() {
    Mockito.when(idpConnectorImpl.findIDPsByTimestamp(LatestTAG.LATEST_SPID.toString()))
        .thenReturn(Optional.of(new ArrayList<>()));

    idpMetadataServiceImpl.refreshPublicIdps();

    Mockito.verify(publicIdpsBucketConnector).uploadIdpsJson(Mockito.anyString(),
        Mockito.eq("[]"));
  }

  @Test
  void isPublicIdpsStatusChange_statusChanged() {
    boolean statusChanged = idpMetadataServiceImpl.isPublicIdpsStatusChange(
        dynamodbEvent("MODIFY", LatestTAG.LATEST_SPID.toString(), "OK", "KO")
            .getRecords().getFirst());

    assertTrue(statusChanged);
  }

  @Test
  void validateDynamodbStatus_invalidStatus() {
    try {
      idpMetadataServiceImpl.validateDynamodbStatus(
          dynamodbEvent("MODIFY", LatestTAG.LATEST_SPID.toString(), "OK", "UNKNOWN")
              .getRecords().getFirst());
      throw new AssertionError("Expected an invalid IDP status to fail the invocation");
    } catch (IllegalArgumentException exception) {
      assertEquals("Invalid IDP status in DynamoDB stream event: UNKNOWN",
          exception.getMessage());
    }

    Mockito.verifyNoInteractions(idpConnectorImpl, publicIdpsBucketConnector);
  }

        @Test
        void publishPublicIdps_spid() {
          ArrayList<IDP> idpMetadata = new ArrayList<>();
          idpMetadata.add(new IDP());

          idpMetadataServiceImpl.publishPublicIdps(idpMetadata, new IdpS3FileDTO("spid-11111.xml"));

          Mockito.verify(publicIdpsBucketConnector).uploadIdpsJson(Mockito.anyString(),
          Mockito.anyString());
        }

  @Test
  void parseIDPMetadata_SPID() {

    // given
    String fileName = "spid-3223456.xml";

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO(fileName);

    ClassLoader classLoader = getClass().getClassLoader();

    StringBuilder stringBuilder = new StringBuilder();

    try (InputStream inputStream = classLoader.getResourceAsStream(fileName);
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader)) {

      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    String content = stringBuilder.toString();

    ArrayList<IDP> idps = idpMetadataServiceImpl.parseIDPMetadata(content, idpS3FileDTO);

    // then
    assertFalse(idps.isEmpty());

  }

  @Test
  void parseIDPMetadata_CIE() {

    // given
    String fileName = "cie-23456.xml";

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO(fileName);

    ClassLoader classLoader = getClass().getClassLoader();

    StringBuilder stringBuilder = new StringBuilder();

    try (InputStream inputStream = classLoader.getResourceAsStream(fileName);
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader)) {

      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    String content = stringBuilder.toString();

    ArrayList<IDP> idps = idpMetadataServiceImpl.parseIDPMetadata(content, idpS3FileDTO);

    // then
    assertFalse(idps.isEmpty());

  }

  @Test
  void parseIDPMetadata_invalidFile() {

    // given
    String fileName = "bogus.xml";

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO("spid-11111.xml");

    ClassLoader classLoader = getClass().getClassLoader();

    StringBuilder stringBuilder = new StringBuilder();

    try (InputStream inputStream = classLoader.getResourceAsStream(fileName);
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader)) {

      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    String content = stringBuilder.toString();

    ArrayList<IDP> idps = idpMetadataServiceImpl.parseIDPMetadata(content, idpS3FileDTO);

    // then
    assertTrue(idps.isEmpty());
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
