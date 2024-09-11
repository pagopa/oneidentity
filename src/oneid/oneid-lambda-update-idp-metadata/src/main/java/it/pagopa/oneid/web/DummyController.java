package it.pagopa.oneid.web;

import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.connector.S3BucketIDPMetadataConnectorImpl;
import it.pagopa.oneid.model.MetadataType;
import it.pagopa.oneid.service.IDPMetadataServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;

// TODO remove this class, this lambda will be triggered by S3 event which will contain the filename to be downloaded
@Path("/")
public class DummyController {

  static final String LATEST_TAG_SPID = "LATEST_SPID";
  static final String LATEST_TAG_CIE = "LATEST_CIE";

  @Inject
  S3BucketIDPMetadataConnectorImpl s3BucketIDPMetadataConnectorImpl;

  @Inject
  IDPMetadataServiceImpl idpMetadataServiceImpl;

  @Inject
  IDPConnectorImpl idpConnectorImpl;

  private static ArrayList<String> getEntityIDs() {
    ArrayList<String> entityIDs = new ArrayList<>();

    entityIDs.add("https://id.eht.eu");
    entityIDs.add("https://id.lepida.it/idp/shibboleth");
    entityIDs.add("https://identity.infocert.it");
    entityIDs.add("https://identity.sieltecloud.it");
    entityIDs.add("https://idp.intesigroup.com");
    entityIDs.add("https://idp.namirialtsp.com/idp");
    entityIDs.add("https://login.id.tim.it/affwebservices/public/saml2sso");
    entityIDs.add("https://loginspid.aruba.it");
    entityIDs.add("https://loginspid.infocamere.it");
    entityIDs.add("https://posteid.poste.it");
    entityIDs.add("https://spid.register.it");
    entityIDs.add("https://spid.teamsystem.com/idp");
    return entityIDs;
  }

  @GET
  @Path("/testSPIDLatest")
  @Produces({MediaType.APPLICATION_XML})
  public Response getDummyResponseSPID() {
    return Response.ok(s3BucketIDPMetadataConnectorImpl.getMetadataFile("spid.xml").get()).build();
  }

  @GET
  @Path("/testCIELatest")
  @Produces({MediaType.APPLICATION_XML})
  public Response getDummyResponseCIE() {
    return Response.ok(s3BucketIDPMetadataConnectorImpl.getMetadataFile("cie.xml").get()).build();
  }


  @GET
  @Path("/testInvoke")
  public Response testInvoke() throws CertificateException {
    String key = "spid-23456.xml";
    String metadataContent = idpMetadataServiceImpl.getMetadataFile(key);
    MetadataType metadataType;
    LatestTAG latestTAG;
    String keyType = key.split("-")[0];
    long keyTimestamp = Long.parseLong(key.split("-")[1].split("\\.")[0]);

    if (keyType.equals("cie")) {
      metadataType = MetadataType.CIE;
      latestTAG = LatestTAG.LATEST_CIE;
    } else {
      metadataType = MetadataType.SPID;
      latestTAG = LatestTAG.LATEST_SPID;
    }

    // Update IDP Metadata table with parsed IDPMetadata information
    idpMetadataServiceImpl.updateIDPMetadata(
        idpMetadataServiceImpl.parseIDPMetadata(metadataContent, keyTimestamp, latestTAG),
        keyTimestamp,
        metadataType);

    ArrayList<String> entityIDs = getEntityIDs();

    final StringBuilder builder = new StringBuilder();

    entityIDs.stream().forEach(entity -> {
      IDP test = idpConnectorImpl.getIDPByEntityIDAndTimestamp(entity,
          LATEST_TAG_CIE).get();

      String certb64 = test.getCertificates().iterator().next();

      byte encodedCert[] = Base64.getMimeDecoder().decode(certb64);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(encodedCert);

      CertificateFactory certFactory = null;
      try {
        certFactory = CertificateFactory.getInstance("X.509");
      } catch (CertificateException e) {
        throw new RuntimeException(e);
      }
      X509Certificate cert = null;
      try {
        cert = (X509Certificate) certFactory.generateCertificate(inputStream);
      } catch (CertificateException e) {
        throw new RuntimeException(e);
      }
      builder.append(cert.getSubjectX500Principal().getName()).append("\n");

    });

    return Response.ok(builder.toString()).build();
  }
}
