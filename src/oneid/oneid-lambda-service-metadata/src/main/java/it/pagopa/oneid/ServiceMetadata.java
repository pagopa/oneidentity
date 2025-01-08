package it.pagopa.oneid;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.enums.IdType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Element;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@CustomLogging
public class ServiceMetadata<T> implements RequestHandler<T, String> {

  @Inject
  Map<String, Client> clientsMap;

  @Inject
  SAMLUtilsExtendedMetadata samlUtils;

  @ConfigProperty(name = "entity_id")
  String ENTITY_ID;

  @ConfigProperty(name = "service_metadata.bucket.name")
  String bucketName;

  @Inject
  S3Client s3;

  public static String getStringValue(Element element) throws SAMLUtilsException {
    StreamResult result = new StreamResult(new StringWriter());
    try {
      TransformerFactory
          .newInstance()
          .newTransformer()
          .transform(new DOMSource(element), result);
    } catch (TransformerException e) {
      throw new SAMLUtilsException(e);
    }
    return result.getWriter().toString();
  }


  @Override
  public String handleRequest(T event, Context context) {
    switch (event) {
      case DynamodbEvent dbEvent -> {
        for (DynamodbStreamRecord record : dbEvent.getRecords()) {
          if (record.getEventName().equals("MODIFY") && !hasMetadataChanged(record)) {
            return "SPID and CIE metadata didn't change";
          }

          processMetadataAndUpload();
        }
      }
      case ScheduledEvent ignored -> processMetadataAndUpload();
      default -> {
        Log.error("Error processing Unknown event type: " + event.getClass());
        throw new RuntimeException();
      }
    }

    return "SPID and CIE metadata uploaded successfully";
  }

  private boolean hasMetadataChanged(DynamodbStreamRecord record) {

    String acsIndexOld = record.getDynamodb().getOldImage().get("acsIndex").getN();
    String acsIndexNew = record.getDynamodb().getNewImage().get("acsIndex").getN();
    if (!acsIndexNew.equals(acsIndexOld)) {
      return true;
    }
    String friendlyNameOld = record.getDynamodb().getOldImage().get("friendlyName").getS();
    String friendlyNameNew = record.getDynamodb().getNewImage().get("friendlyName").getS();
    if (!friendlyNameNew.equals(friendlyNameOld)) {
      return true;
    }

    List<String> requestedParametersOld = record.getDynamodb().getOldImage()
        .get("requestedParameters").getSS();
    List<String> requestedParametersNew = record.getDynamodb().getNewImage()
        .get("requestedParameters").getSS();
    if (!requestedParametersNew.equals(requestedParametersOld)) {
      return true;
    }
    String authLevelOld = record.getDynamodb().getOldImage().get("authLevel").getS();
    String authLevelNew = record.getDynamodb().getNewImage().get("authLevel").getS();
    if (!authLevelNew.equals(authLevelOld)) {
      return true;
    }
    String attributeIndexOld = record.getDynamodb().getOldImage().get("attributeIndex")
        .getN();
    String attributeIndexNew = record.getDynamodb().getNewImage().get("attributeIndex")
        .getN();
    if (!attributeIndexNew.equals(attributeIndexOld)) {
      return true;
    }
    boolean isActiveOld = record.getDynamodb().getOldImage().get("active").getBOOL();
    boolean isActiveNew = record.getDynamodb().getNewImage().get("active").getBOOL();
    return isActiveNew != isActiveOld;
  }

  private void uploadToS3(String objectKey, String content) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .contentType(MediaType.APPLICATION_XML)
        .key(objectKey)
        .build();

    try {
      s3.putObject(putObjectRequest,
          RequestBody.fromString(content));
    } catch (S3Exception e) {
      Log.error("error during s3 putObject: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void processMetadataAndUpload() {

    try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
      String spidMetadata = generateMetadata(IdType.spid);
      String cieMetadata = generateMetadata(IdType.cie);

      executorService.submit(() -> uploadToS3("spid.xml", spidMetadata));
      executorService.submit(() -> uploadToS3("cie.xml", cieMetadata));
    } catch (Exception e) {
      Log.error("Error processing event: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private String generateMetadata(IdType idType) throws OneIdentityException {

    EntityDescriptor entityDescriptor = samlUtils.buildSAMLObject(EntityDescriptor.class);
    entityDescriptor.setEntityID(ENTITY_ID);

    SPSSODescriptor spssoDescriptor = samlUtils.buildSPSSODescriptor();
    spssoDescriptor.getKeyDescriptors().add(samlUtils.buildKeyDescriptor());
    spssoDescriptor.getNameIDFormats().add(samlUtils.buildNameIDFormat());
    spssoDescriptor.getAssertionConsumerServices().add(samlUtils.buildAssertionConsumerService());
    spssoDescriptor.getSingleLogoutServices().add(samlUtils.buildSingleLogoutService());
    spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

    for (Client client : clientsMap.values()) {
      spssoDescriptor.getAttributeConsumingServices()
          .add(samlUtils.buildAttributeConsumingService(client));
    }

    entityDescriptor.setOrganization(samlUtils.buildOrganization());
    entityDescriptor.getContactPersons()
        .add(samlUtils.buildContactPerson(idType.getNamespacePrefix(), idType.getNamespaceUri()));
    entityDescriptor.getRoleDescriptors().add(spssoDescriptor);
    entityDescriptor.getNamespaceManager()
        .registerNamespaceDeclaration(
            new Namespace(idType.getNamespaceUri(), idType.getNamespacePrefix()));

    Signature signature = samlUtils.buildSignature(entityDescriptor);
    entityDescriptor.setSignature(signature);
    Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory()
        .getMarshaller(entityDescriptor);

    Element plaintextElement;
    try {
      plaintextElement = out.marshall(entityDescriptor);
    } catch (MarshallingException e) {
      throw new OneIdentityException(e);
    }

    try {
      Signer.signObject(signature);
    } catch (SignatureException e) {
      throw new OneIdentityException(e);
    }

    return getStringValue(plaintextElement);
  }
}
