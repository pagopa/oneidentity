package it.pagopa.oneid;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.enums.IdType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

  static {
    System.setProperty("org.apache.xml.security.ignoreLineBreaks", "true");
    org.apache.xml.security.Init.init();
  }

  @Inject
  ClientConnectorImpl clientConnectorImpl;
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

      // ScheduledEvent
      processMetadataAndUpload();
      Log.info("SPID and CIE metadata uploaded successfully");

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return "SPID and CIE metadata uploaded successfully";
  }

  private boolean hasMetadataChanged(JsonNode nodeRecord) {
    if (hasDynamoScalarFieldChanged(nodeRecord, "acsIndex", "N")) {
      return true;
    }
    if (hasDynamoScalarFieldChanged(nodeRecord, "friendlyName", "S")) {
      return true;
    }
    if (hasDynamoStringSetFieldChanged(nodeRecord, "requestedParameters")) {
      return true;
    }
    if (hasDynamoScalarFieldChanged(nodeRecord, "authLevel", "S")) {
      return true;
    }
    if (hasDynamoScalarFieldChanged(nodeRecord, "attributeIndex", "N")) {
      return true;
    }
    if (hasDynamoScalarFieldChanged(nodeRecord, "active", "BOOL")) {
      return true;
    }
    if (hasDynamoScalarFieldChanged(nodeRecord, "spidMinors", "BOOL")) {
      return true;
    }
    if (hasDynamoScalarFieldChanged(nodeRecord, "minAge", "N")) {
      return true;
    }
    if (hasDynamoScalarFieldChanged(nodeRecord, "maxAge", "N")) {
      return true;
    }
    if (hasDynamoScalarFieldChanged(nodeRecord, "ageParentAuth", "N")) {
      return true;
    }
    return false;
  }

  private boolean hasDynamoScalarFieldChanged(JsonNode nodeRecord, String fieldName, String type) {
    return hasDynamoScalarFieldChanged(nodeRecord, fieldName, type, null);
  }

  private boolean hasDynamoScalarFieldChanged(JsonNode nodeRecord, String fieldName, String type,
      String defaultValue) {
    JsonNode oldImage = nodeRecord.get("dynamodb").get("OldImage");
    JsonNode newImage = nodeRecord.get("dynamodb").get("NewImage");

    String oldFieldValue = extractScalarFieldValue(oldImage.get(fieldName), type, defaultValue);
    String newFieldValue = extractScalarFieldValue(newImage.get(fieldName), type, defaultValue);
    return !Objects.equals(oldFieldValue, newFieldValue);
  }

  private boolean hasDynamoStringSetFieldChanged(JsonNode nodeRecord, String fieldName) {
    JsonNode oldImage = nodeRecord.get("dynamodb").get("OldImage");
    JsonNode newImage = nodeRecord.get("dynamodb").get("NewImage");

    return !extractStringSetFieldValues(oldImage.get(fieldName))
        .equals(extractStringSetFieldValues(newImage.get(fieldName)));
  }

  private String extractScalarFieldValue(JsonNode fieldNode, String type, String defaultValue) {
    if (fieldNode == null || fieldNode.get(type) == null || fieldNode.get(type).isNull()) {
      return defaultValue;
    }
    String value = fieldNode.get(type).asText();
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    return value;
  }

  private List<String> extractStringSetFieldValues(JsonNode fieldNode) {
    List<String> values = new ArrayList<>();
    if (fieldNode == null || fieldNode.get("SS") == null) {
      return values;
    }
    fieldNode.get("SS").forEach(value -> values.add(value.asText()));
    return values;
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

      // Generate metadata for SPID and CIE in parallel
      CompletableFuture<String> spidMetadataFuture = CompletableFuture.supplyAsync(() -> {
        try {
          return generateMetadata(IdType.spid);
        } catch (OneIdentityException e) {
          throw new CompletionException(e);
        }
      }, executorService);
      CompletableFuture<String> cieMetadataFuture = CompletableFuture.supplyAsync(() -> {
        try {
          return generateMetadata(IdType.cie);
        } catch (OneIdentityException e) {
          throw new CompletionException(e);
        }
      }, executorService);

      // Upload spidMetadata to S3 once generated
      CompletableFuture<Void> spidUpload = spidMetadataFuture.thenAcceptAsync(spidMetadata -> {
        uploadToS3("spid.xml", spidMetadata);
      }, executorService);

      // Upload cieMetadata to S3 once generated
      CompletableFuture<Void> cieUpload = cieMetadataFuture.thenAcceptAsync(cieMetadata -> {
        uploadToS3("cie.xml", cieMetadata);
      }, executorService);

      // Wait for both uploads to finish
      CompletableFuture.allOf(spidUpload, cieUpload).join();

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
    spssoDescriptor.getSingleLogoutServices().add(samlUtils.buildSingleLogoutService());
    spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

    Map<String, Client> clientsMap = getClientsMap();

    boolean firstAcs = true;
    java.util.Set<Integer> addedAcsIndices = new java.util.HashSet<>();
    for (Client client : clientsMap.values()) {
      int acsIndex = client.getAcsIndex();
      if (addedAcsIndices.add(acsIndex)) {
        spssoDescriptor.getAssertionConsumerServices()
            .add(samlUtils.buildAssertionConsumerService(acsIndex, firstAcs));
        firstAcs = false;
      }
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

    org.opensaml.saml.saml2.metadata.Extensions entityExtensions = samlUtils.buildEntityExtensions(
        clientsMap);
    if (entityExtensions != null) {
      entityDescriptor.setExtensions(entityExtensions);
      if (!idType.getNamespacePrefix().equals("spid")) {
        entityDescriptor.getNamespaceManager()
            .registerNamespaceDeclaration(
                new Namespace("https://spid.gov.it/saml-extensions", "spid"));
      }
    }

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
      Signer.signObject(Objects.requireNonNull(signature));
    } catch (SignatureException e) {
      throw new OneIdentityException(e);
    }

    return getStringValue(plaintextElement);
  }

  private Map<String, Client> getClientsMap() {
    Map<String, Client> map = new HashMap<>();
    ArrayList<Client> clients = clientConnectorImpl.findAllActive().orElse(new ArrayList<>());

    clients.forEach(client -> map.put(client.getClientId(), client));

    return map;
  }
}
