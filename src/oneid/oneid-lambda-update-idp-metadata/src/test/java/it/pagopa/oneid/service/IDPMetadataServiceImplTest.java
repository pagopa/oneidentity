package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.model.enums.MetadataType;
import it.pagopa.oneid.connector.S3BucketIDPMetadataConnectorImpl;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class IDPMetadataServiceImplTest {

  @InjectMock
  S3BucketIDPMetadataConnectorImpl s3BucketIDPMetadataConnector;

  @InjectMock
  IDPConnectorImpl idpConnectorImpl;

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
    Mockito.doNothing().when(idpConnectorImpl).saveIDPs(Mockito.any(), Mockito.any());

    // then
    ArrayList<IDP> idpList = new ArrayList<>();
    idpList.add(new IDP());

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO("spid-11111.xml");
    idpS3FileDTO.setLatestTAG(LatestTAG.LATEST_SPID);

    // then
    assertDoesNotThrow(() -> idpMetadataServiceImpl.updateIDPMetadata(idpList, idpS3FileDTO));
  }

  @Test
  void parseIDPMetadata_SPID() {

    // given
    String fileName = "spid-3223456.xml";

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO(fileName);
    idpS3FileDTO.setMetadataType(MetadataType.SPID);
    idpS3FileDTO.setLatestTAG(LatestTAG.LATEST_SPID);
    idpS3FileDTO.setTimestamp(System.currentTimeMillis());

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
      e.printStackTrace();
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
    idpS3FileDTO.setMetadataType(MetadataType.CIE);
    idpS3FileDTO.setLatestTAG(LatestTAG.LATEST_CIE);
    idpS3FileDTO.setTimestamp(System.currentTimeMillis());

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
      e.printStackTrace();
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

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO(MetadataType.SPID,
        System.currentTimeMillis(), LatestTAG.LATEST_SPID);

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
      e.printStackTrace();
    }

    String content = stringBuilder.toString();

    ArrayList<IDP> idps = idpMetadataServiceImpl.parseIDPMetadata(content, idpS3FileDTO);

    // then
    assertTrue(idps.isEmpty());
  }
}
