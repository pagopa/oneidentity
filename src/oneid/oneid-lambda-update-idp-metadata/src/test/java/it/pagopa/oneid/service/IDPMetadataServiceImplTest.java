package it.pagopa.oneid.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
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
  void publishPublicIdps_spid() {
    IDP firstIdp = new IDP();
    IDP secondIdp = new IDP();
    ArrayList<IDP> idpMetadata = new ArrayList<>(java.util.List.of(firstIdp, secondIdp));

    Mockito.doNothing().when(publicIdpsBucketConnector)
        .uploadIdpsJson(Mockito.anyString(), Mockito.anyString());

    idpMetadataServiceImpl.publishPublicIdps(idpMetadata, new IdpS3FileDTO("spid-11111.xml"));

    Mockito.verify(publicIdpsBucketConnector).uploadIdpsJson(Mockito.anyString(),
      Mockito.anyString());
  }

  @Test
  void publishPublicIdps_cie_skipsUpload() {
    idpMetadataServiceImpl.publishPublicIdps(new ArrayList<>(), new IdpS3FileDTO("cie-11111.xml"));

    Mockito.verifyNoInteractions(publicIdpsBucketConnector);
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
}
