package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.connector.PublicIdpsBucketConnector;
import it.pagopa.oneid.connector.S3BucketIDPMetadataConnectorImpl;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    when(s3BucketIDPMetadataConnector.getMetadataFile(any())).thenReturn("dummy");

    // then
    assertEquals("dummy", idpMetadataServiceImpl.getMetadataFile("foobar"));
  }

  @Test
  void updateIDPMetadata() {
    doNothing().when(idpConnectorImpl).saveIDPs(any(), any(), any());

    // then
    ArrayList<IDP> idpList = new ArrayList<>();
    idpList.add(new IDP());

    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO("spid-11111.xml");

    // then
    assertDoesNotThrow(() -> idpMetadataServiceImpl.updateIDPMetadata(idpList, idpS3FileDTO));
  }

  @Test
  void updateIDPMetadata_eidas() {
    doNothing().when(idpConnectorImpl).saveIDPs(any(), any(), any());

    ArrayList<IDP> idpList = new ArrayList<>();
    idpList.add(new IDP());
    IdpS3FileDTO idpS3FileDTO = new IdpS3FileDTO("eidas-11111.xml");

    idpMetadataServiceImpl.updateIDPMetadata(idpList, idpS3FileDTO);

    verify(idpConnectorImpl).saveIDPs(idpList, LatestTAG.LATEST_EIDAS, "11111");
  }

  @Test
  void refreshPublicIdps() {
    when(idpConnectorImpl.findIDPsByTimestamp(LatestTAG.LATEST_SPID.toString()))
        .thenReturn(Optional.of(new ArrayList<>()));

    idpMetadataServiceImpl.refreshPublicIdps();

    verify(publicIdpsBucketConnector).uploadIdpsJson(anyString(), eq("[]"));
  }

  @Test
  void isPublicIdpsStatusChange_statusChanged() {
    boolean snapshotChanged = idpMetadataServiceImpl.isPublicIdpsStatusChange(
        dynamodbRecord("MODIFY", LatestTAG.LATEST_SPID.toString(), "OK", "KO", true, true));

    assertTrue(snapshotChanged);
  }

  @Test
  void isPublicIdpsActiveChange_activeChanged() {
    boolean snapshotChanged = idpMetadataServiceImpl.isPublicIdpsActiveChange(
        dynamodbRecord("MODIFY", LatestTAG.LATEST_SPID.toString(), "OK", "OK", false, true));

    assertTrue(snapshotChanged);
  }

  @Test
  void isPublicIdpsActiveChange_unchanged() {
    boolean snapshotChanged = idpMetadataServiceImpl.isPublicIdpsActiveChange(
        dynamodbRecord("MODIFY", LatestTAG.LATEST_SPID.toString(), "OK", "OK", true, true));

    assertFalse(snapshotChanged);
  }

  @Test
  void isPublicIdpsStatusChange_unchanged() {
    boolean snapshotChanged = idpMetadataServiceImpl.isPublicIdpsStatusChange(
        dynamodbRecord("MODIFY", LatestTAG.LATEST_SPID.toString(), "OK", "OK", true, true));

    assertFalse(snapshotChanged);
  }

  @Test
  void isPublicIdpsStatusChange_invalidManualStatus() {
    boolean snapshotChanged = idpMetadataServiceImpl.isPublicIdpsStatusChange(
        dynamodbRecord("MODIFY", LatestTAG.LATEST_SPID.toString(), "OK", "UNKNOWN", true,
            true));

    assertTrue(snapshotChanged);
  }

  @Test
  void publishPublicIdps_spid() {
    ArrayList<IDP> idpMetadata = new ArrayList<>();
    IDP idp = new IDP();
    idp.setPointer(LatestTAG.LATEST_SPID.toString());
    idpMetadata.add(idp);

    idpMetadataServiceImpl.publishPublicIdps(idpMetadata, new IdpS3FileDTO("spid-11111.xml"));

    verify(publicIdpsBucketConnector).uploadIdpsJson(anyString(), anyString());
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
  void parseIDPMetadata_EIDAS() {

    // given
    String fileName = "eidas-12345.xml";

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
    assertEquals(1, idps.size());
    IDP eidasIdp = idps.get(0);
    assertEquals("https://sp-proxy.pre.eid.gov.it/spproxy/idpit", eidasIdp.getEntityID());
    assertEquals(LatestTAG.LATEST_EIDAS.toString(), eidasIdp.getPointer());
    assertEquals("Agenzia per l'Italia Digitale - AgID", eidasIdp.getFriendlyName());
    assertFalse(eidasIdp.getCertificates().isEmpty());
    assertNotNull(
        eidasIdp.getIdpSSOEndpoints().get("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"));
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

  @Test
  @DisplayName("given legacy metadata wrappers when parsing then preserve namespace compatibility")
  void given_legacyMetadata_when_parsing_then_acceptsUnqualifiedElements() {
    IDPMetadataServiceImpl service = service(eidasEntityIds().get(0));
    String unqualifiedEntity = entity("https://spid.example");
    String qualifiedEntity = unqualifiedEntity.replace("<EntityDescriptor ",
        "<EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" ");

    for (String descriptor : List.of(unqualifiedEntity, qualifiedEntity)) {
      String xml = "<EntitiesDescriptor><Signature/><Extensions/>"
          + descriptor + "</EntitiesDescriptor>";

      ArrayList<IDP> idps = service.parseIDPMetadata(xml, new IdpS3FileDTO("spid-11111.xml"));

      assertEquals(1, idps.size());
      assertEquals("https://spid.example", idps.get(0).getEntityID());
      assertEquals(LatestTAG.LATEST_SPID.toString(), idps.get(0).getPointer());
    }
    verifyNoInteractions(service.idpConnectorImpl, service.publicIdpsBucketConnector);
  }

  @Test
  @DisplayName("given pre and prod eIDAS IDs when parsing SPID then classify only exact matches")
  void given_eidasEntityIds_when_parsingSpid_then_classifiesExactMatch() {
    for (String entityId : eidasEntityIds()) {
      IDPMetadataServiceImpl service = service(entityId);
      String xml = metadata(entityId, entityId + "/other", "https://spid.example");

      ArrayList<IDP> idps = service.parseIDPMetadata(xml, new IdpS3FileDTO("spid-11111.xml"));

      assertEquals(3, idps.size());
      assertEquals(LatestTAG.LATEST_EIDAS.toString(), idps.get(0).getPointer());
      assertEquals(LatestTAG.LATEST_SPID.toString(), idps.get(1).getPointer());
      assertEquals(LatestTAG.LATEST_SPID.toString(), idps.get(2).getPointer());
      assertEquals(entityId, idps.get(0).getEntityID());
    }
  }

  @Test
  @DisplayName("given a mixed SPID document when saving then save separate coherent groups")
  void given_mixedSpid_when_saving_then_savesSeparateGroups() {
    for (String entityId : eidasEntityIds()) {
      IDPMetadataServiceImpl service = service(entityId);
      IdpS3FileDTO file = new IdpS3FileDTO("spid-11111.xml");
      String xml = metadata("https://spid.example", entityId);
      ArrayList<IDP> idps = service.parseIDPMetadata(xml, file);

      service.updateIDPMetadata(idps, file);

      verify(service.idpConnectorImpl).saveIDPs(new ArrayList<>(List.of(idps.get(0))),
          LatestTAG.LATEST_SPID, "11111");
      verify(service.idpConnectorImpl).saveIDPs(new ArrayList<>(List.of(idps.get(1))),
          LatestTAG.LATEST_EIDAS, "11111");
      assertEquals(LatestTAG.LATEST_SPID.toString(), idps.get(0).getPointer());
      assertEquals(LatestTAG.LATEST_EIDAS.toString(), idps.get(1).getPointer());
      verifyNoMoreInteractions(service.idpConnectorImpl);
      assertEquals(metadata("https://spid.example", entityId), xml);
    }
  }

  @Test
  @DisplayName("given an absent group when saving SPID then never archive the absent group")
  void given_absentGroup_when_savingSpid_then_skipsAbsentGroup() {
    String entityId = eidasEntityIds().get(0);
    for (String onlyEntity : List.of(entityId, "https://spid.example")) {
      IDPMetadataServiceImpl service = service(entityId);
      IdpS3FileDTO file = new IdpS3FileDTO("spid-11111.xml");
      ArrayList<IDP> idps = service.parseIDPMetadata(metadata(onlyEntity), file);

      service.updateIDPMetadata(idps, file);

      LatestTAG expected = onlyEntity.equals(entityId)
          ? LatestTAG.LATEST_EIDAS : LatestTAG.LATEST_SPID;
      verify(service.idpConnectorImpl).saveIDPs(idps, expected, "11111");
      verifyNoMoreInteractions(service.idpConnectorImpl);
    }
  }

  @Test
  @DisplayName("given saved standalone eIDAS when SPID lacks eIDAS then do not overwrite eIDAS")
  void given_standaloneEidas_when_spidLacksEidas_then_preservesEidas() {
    for (String entityId : eidasEntityIds()) {
      IDPMetadataServiceImpl service = service(entityId);
      IdpS3FileDTO eidasFile = new IdpS3FileDTO("eidas-11111.xml");
      ArrayList<IDP> eidasIdps = service.parseIDPMetadata(metadata(entityId), eidasFile);
      service.updateIDPMetadata(eidasIdps, eidasFile);
      IdpS3FileDTO spidFile = new IdpS3FileDTO("spid-22222.xml");
      ArrayList<IDP> spidIdps = service.parseIDPMetadata(metadata("https://spid.example"), spidFile);

      service.updateIDPMetadata(spidIdps, spidFile);

      verify(service.idpConnectorImpl).saveIDPs(eidasIdps, LatestTAG.LATEST_EIDAS, "11111");
      verify(service.idpConnectorImpl).saveIDPs(spidIdps, LatestTAG.LATEST_SPID, "22222");
      verifyNoMoreInteractions(service.idpConnectorImpl);
    }
  }

  @Test
  @DisplayName("given standalone eIDAS when saving then persist eIDAS without public SPID snapshot")
  void given_standaloneEidas_when_saving_then_persistsWithoutPublicSnapshot() {
    for (String entityId : eidasEntityIds()) {
      IDPMetadataServiceImpl service = service(entityId);
      IdpS3FileDTO file = new IdpS3FileDTO("eidas-11111.xml");
      ArrayList<IDP> idps = service.parseIDPMetadata(entity(entityId), file);

      service.updateIDPMetadata(idps, file);
      service.publishPublicIdps(idps, file);

      assertEquals(LatestTAG.LATEST_EIDAS.toString(), idps.get(0).getPointer());
      verify(service.idpConnectorImpl).saveIDPs(idps, LatestTAG.LATEST_EIDAS, "11111");
      verifyNoMoreInteractions(service.idpConnectorImpl);
      verifyNoInteractions(service.publicIdpsBucketConnector);
    }
  }

  @Test
  @DisplayName("given CIE or standalone eIDAS when saving then preserve the original group")
  void given_nonSpidMetadata_when_saving_then_preservesGroup() {
    for (String entityId : eidasEntityIds()) {
      for (String type : List.of("cie", "eidas")) {
        IDPMetadataServiceImpl service = service(entityId);
        IdpS3FileDTO file = new IdpS3FileDTO(type + "-11111.xml");
        ArrayList<IDP> idps = service.parseIDPMetadata(metadata(entityId), file);

        service.updateIDPMetadata(idps, file);
        service.publishPublicIdps(idps, file);

        assertEquals(file.getLatestTAG().toString(), idps.get(0).getPointer());
        verify(service.idpConnectorImpl).saveIDPs(idps, file.getLatestTAG(), "11111");
        verifyNoMoreInteractions(service.idpConnectorImpl);
        verifyNoInteractions(service.publicIdpsBucketConnector);
      }
    }
  }

  @Test
  @DisplayName("given mixed metadata when publishing and refreshing then expose only SPID")
  void given_mixedMetadata_when_publishingAndRefreshing_then_excludesEidas() throws Exception {
    for (String entityId : eidasEntityIds()) {
      IDPMetadataServiceImpl service = service(entityId);
      IdpS3FileDTO file = new IdpS3FileDTO("spid-11111.xml");
      ArrayList<IDP> idps = service.parseIDPMetadata(metadata("https://spid.example", entityId), file);
      idps.get(0).setActive(false);
      String expected = service.objectMapper.writeValueAsString(List.of(idps.get(0)));

      service.publishPublicIdps(idps, file);

      verify(service.publicIdpsBucketConnector).uploadIdpsJson("idps.json", expected);
      idps.get(1).setPointer(LatestTAG.LATEST_SPID.toString());
      when(service.idpConnectorImpl.findIDPsByTimestamp(LatestTAG.LATEST_SPID.toString()))
          .thenReturn(Optional.of(idps));

      service.refreshPublicIdps();

      verify(service.publicIdpsBucketConnector, org.mockito.Mockito.times(2))
          .uploadIdpsJson("idps.json", expected);
      assertFalse(idps.get(0).isActive());
    }
  }

  @Test
  @DisplayName("given only eIDAS in SPID when publishing then do not publish eIDAS")
  void given_onlyEidas_when_publishing_then_skipsSnapshot() {
    IDPMetadataServiceImpl service = service(eidasEntityIds().get(0));
    IdpS3FileDTO file = new IdpS3FileDTO("spid-11111.xml");

    service.publishPublicIdps(service.parseIDPMetadata(metadata(eidasEntityIds().get(0)), file), file);

    verifyNoInteractions(service.publicIdpsBucketConnector);
  }

  @Test
  @DisplayName("given empty metadata when saving then retain the main branch behavior")
  void given_emptyMetadata_when_saving_then_preservesExistingFlow() {
    for (String type : List.of("spid", "cie", "eidas")) {
      IDPMetadataServiceImpl service = service(eidasEntityIds().get(0));
      IdpS3FileDTO file = new IdpS3FileDTO(type + "-11111.xml");
      ArrayList<IDP> idps = new ArrayList<>();

      service.updateIDPMetadata(idps, file);

      verify(service.idpConnectorImpl).saveIDPs(idps, file.getLatestTAG(), "11111");
      verifyNoMoreInteractions(service.idpConnectorImpl);
    }
  }

  private IDPMetadataServiceImpl service(String entityId) {
    IDPMetadataServiceImpl service = new IDPMetadataServiceImpl();
    service.eidasEntityId = entityId;
    service.idpConnectorImpl = mock(IDPConnectorImpl.class);
    service.publicIdpsBucketConnector = mock(PublicIdpsBucketConnector.class);
    service.objectMapper = new ObjectMapper();
    service.publicIdpsObjectKey = "idps.json";
    return service;
  }

  private List<String> eidasEntityIds() {
    return List.of("https://sp-proxy.pre.eid.gov.it/spproxy/idpit",
        "https://sp-proxy.eid.gov.it/spproxy/idpit");
  }

  private String metadata(String... entityIds) {
    StringBuilder xml = new StringBuilder(
        "<EntitiesDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\">");
    for (String entityId : entityIds) {
      xml.append(entity(entityId));
    }
    return xml.append("</EntitiesDescriptor>").toString();
  }

  private String entity(String entityId) {
    return """
        <EntityDescriptor entityID="%s">
          <IDPSSODescriptor>
            <KeyDescriptor use="signing"><X509Certificate>certificate</X509Certificate></KeyDescriptor>
            <SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
                Location="https://sso.example"/>
          </IDPSSODescriptor>
        </EntityDescriptor>
        """.formatted(entityId);
  }

    private JsonNode dynamodbRecord(String eventName, String pointer, String oldStatus,
      String newStatus, boolean oldActive, boolean newActive) {
    try {
      return new ObjectMapper().readTree("""
          {
            "eventName": "%s",
            "dynamodb": {
              "OldImage": {
                "pointer": { "S": "%s" },
                "status": { "S": "%s" },
                "active": { "BOOL": %s }
              },
              "NewImage": {
                "pointer": { "S": "%s" },
                "status": { "S": "%s" },
                "active": { "BOOL": %s }
              }
            }
          }
          """.formatted(eventName, pointer, oldStatus, oldActive, pointer, newStatus,
          newActive));
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }
}
