package it.pagopa.oneid;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.enums.IdType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class ServiceMetadata implements RequestHandler<Object, String> {

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
  public String handleRequest(Object event, Context context) {

    try {
      ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String json = ow.writeValueAsString(event);
      JsonMapper mapper = JsonMapper.builder().build();
      JsonNode eventNode = mapper.readTree(json);

      // DynamodbEvent
      JsonNode records = eventNode.get("Records");
      if (records != null) {
        records.forEach(record -> {
          if (record.get("eventName").asText().equals("MODIFY") && !hasMetadataChanged(record)) {
            Log.info("SPID and CIE metadata didn't change");
          } else {
            processMetadataAndUpload();
            Log.info("SPID and CIE metadata uploaded successfully");
          }
        });
        return "SPID and CIE metadata uploaded successfully";
      }

      //ScheduledEvent
      processMetadataAndUpload();
      Log.info("SPID and CIE metadata uploaded successfully");


    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return "SPID and CIE metadata uploaded successfully";
  }


  private boolean hasMetadataChanged(JsonNode nodeRecord) {

    String acsIndexOld = nodeRecord.get("dynamodb").get("OldImage").get("acsIndex").get("N")
        .asText();
    String acsIndexNew = nodeRecord.get("dynamodb").get("NewImage").get("acsIndex").get("N")
        .asText();
    if (!acsIndexNew.equals(acsIndexOld)) {
      return true;
    }
    String friendlyNameOld = nodeRecord.get("dynamodb").get("OldImage").get("friendlyName").get("S")
        .asText();
    String friendlyNameNew = nodeRecord.get("dynamodb").get("NewImage").get("friendlyName").get("S")
        .asText();
    if (!friendlyNameNew.equals(friendlyNameOld)) {
      return true;
    }

    List<String> requestedParametersOld = new ArrayList<>();
    nodeRecord.get("dynamodb").get("OldImage")
        .get("requestedParameters").get("SS").forEach(parameterOld -> {
          requestedParametersOld.add(parameterOld.asText());
        });
    List<String> requestedParametersNew = new ArrayList<>();
    nodeRecord.get("dynamodb").get("NewImage")
        .get("requestedParameters").get("SS").forEach(parameterNew -> {
          requestedParametersNew.add(parameterNew.asText());
        });
    if (!requestedParametersNew.equals(requestedParametersOld)) {
      return true;
    }
    String authLevelOld = nodeRecord.get("dynamodb").get("OldImage").get("authLevel").get("S")
        .asText();
    String authLevelNew = nodeRecord.get("dynamodb").get("NewImage").get("authLevel").get("S")
        .asText();
    if (!authLevelNew.equals(authLevelOld)) {
      return true;
    }
    String attributeIndexOld = nodeRecord.get("dynamodb").get("OldImage").get("attributeIndex")
        .get("N")
        .asText();
    String attributeIndexNew = nodeRecord.get("dynamodb").get("NewImage").get("attributeIndex")
        .get("N")
        .asText();
    if (!attributeIndexNew.equals(attributeIndexOld)) {
      return true;
    }
    boolean isActiveOld = nodeRecord.get("dynamodb").get("OldImage").get("active").get("BOOL")
        .asBoolean();
    boolean isActiveNew = nodeRecord.get("dynamodb").get("NewImage").get("active").get("BOOL")
        .asBoolean();
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
      plaintextElement = Objects.requireNonNull(out).marshall(entityDescriptor);
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
