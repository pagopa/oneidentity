package it.pagopa.oneid.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.common.model.enums.IDPStatus;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.connector.PublicIdpsBucketConnector;
import it.pagopa.oneid.connector.S3BucketIDPMetadataConnectorImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@ApplicationScoped
@CustomLogging
public class IDPMetadataServiceImpl implements IDPMetadataService {

  @Inject
  S3BucketIDPMetadataConnectorImpl s3BucketIDPMetadataConnector;

  @Inject
  IDPConnectorImpl idpConnectorImpl;

  @Inject
  PublicIdpsBucketConnector publicIdpsBucketConnector;

  @Inject
  ObjectMapper objectMapper;

  @ConfigProperty(name = "public_idps.object.key")
  String publicIdpsObjectKey;

  @Override
  public ArrayList<IDP> parseIDPMetadata(String idpMetadata, IdpS3FileDTO idpS3FileDTO) {
    ArrayList<IDP> idpList = new ArrayList<>();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(idpMetadata));
      Document doc = builder.parse(is);
      doc.getDocumentElement().normalize();

      NodeList entityDescriptors = doc.getElementsByTagNameNS("*", "EntityDescriptor");
      for (int entityIndex = 0; entityIndex < entityDescriptors.getLength(); entityIndex++) {
        Node entityDescriptor = entityDescriptors.item(entityIndex);
        if (entityDescriptor.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }

        idpList.add(parseEntityDescriptor((Element) entityDescriptor, idpS3FileDTO));
      }
    } catch (Exception e) {
      Log.error("error parsing IDP metadata " + ExceptionUtils.getStackTrace(e));
    }

    return idpList;
  }

  @Override
  public void updateIDPMetadata(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO) {
    idpConnectorImpl.saveIDPs(idpMetadata, idpS3FileDTO.getLatestTAG(),
        String.valueOf(idpS3FileDTO.getTimestamp()));
  }

  @Override
  public void publishPublicIdps(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO) {
    if (!idpS3FileDTO.toString().startsWith("spid-")) {
      Log.info("Skipping public IDPs snapshot for non-SPID metadata: file=" + idpS3FileDTO);
      return;
    }

    if (idpMetadata.isEmpty()) {
      Log.warn("IDPs not found for public snapshot publish");
      return;
    }

    Log.info("Publishing public IDPs snapshot: key=" + publicIdpsObjectKey + ", count="
      + idpMetadata.size());
    publicIdpsBucketConnector.uploadIdpsJson(publicIdpsObjectKey,
        serializePublicIdps(idpMetadata));
  }

  @Override
  public void refreshPublicIdps() {
    ArrayList<IDP> idps = idpConnectorImpl
        .findIDPsByTimestamp(LatestTAG.LATEST_SPID.toString())
        .orElseGet(ArrayList::new);
    Log.info("Refreshing public IDPs snapshot: key=" + publicIdpsObjectKey + ", count="
      + idps.size());
    publicIdpsBucketConnector.uploadIdpsJson(publicIdpsObjectKey, serializePublicIdps(idps));
  }

  @Override
  public String getMetadataFile(String fileName) {
    return s3BucketIDPMetadataConnector.getMetadataFile(fileName);
  }

  @Override
  public boolean isPublicIdpsStatusChange(JsonNode dynamodbEventRecord) {
    if (!isLatestSpidModify(dynamodbEventRecord)) {
      return false;
    }

    JsonNode newImage = dynamodbEventRecord.path("dynamodb").path("NewImage");
    JsonNode oldImage = dynamodbEventRecord.path("dynamodb").path("OldImage");
    String newStatus = readStringAttribute(newImage, "status");
    String oldStatus = readStringAttribute(oldImage, "status");

    return !Objects.equals(newStatus, oldStatus);
  }

  @Override
  public boolean isPublicIdpsActiveChange(JsonNode dynamodbEventRecord) {
    if (!isLatestSpidModify(dynamodbEventRecord)) {
      return false;
    }

    JsonNode newImage = dynamodbEventRecord.path("dynamodb").path("NewImage");
    JsonNode oldImage = dynamodbEventRecord.path("dynamodb").path("OldImage");
    Boolean oldActive = readBooleanAttribute(oldImage, "active");
    Boolean newActive = readBooleanAttribute(newImage, "active");

    return !Objects.equals(newActive, oldActive);
  }

  private IDP parseEntityDescriptor(Element entityDescriptor, IdpS3FileDTO idpS3FileDTO) {
    IDP idp = new IDP();
    idp.setPointer(String.valueOf(idpS3FileDTO.getLatestTAG()));
    idp.setStatus(IDPStatus.OK);
    idp.setActive(true);
    idp.setEntityID(entityDescriptor.getAttribute("entityID"));
    populateSsoData(idp, entityDescriptor);
    populateFriendlyName(idp, entityDescriptor);
    return idp;
  }

  private void populateSsoData(IDP idp, Element entityDescriptor) {
    NodeList ssoDescriptors = entityDescriptor.getElementsByTagNameNS("*", "IDPSSODescriptor");
    for (int descriptorIndex = 0; descriptorIndex < ssoDescriptors.getLength(); descriptorIndex++) {
      idp.setCertificates(readSigningCertificates(entityDescriptor));
      idp.setIdpSSOEndpoints(readSsoEndpoints(entityDescriptor));
    }
  }

  private Set<String> readSigningCertificates(Element entityDescriptor) {
    Set<String> certificates = new HashSet<>();
    NodeList keyDescriptors = entityDescriptor.getElementsByTagNameNS("*", "KeyDescriptor");
    for (int keyIndex = 0; keyIndex < keyDescriptors.getLength(); keyIndex++) {
      Node keyNode = keyDescriptors.item(keyIndex);
      if (keyNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }

      Element keyDescriptor = (Element) keyNode;
      if (!"signing".equals(keyDescriptor.getAttribute("use"))) {
        continue;
      }

      Node certificate = keyDescriptor.getElementsByTagNameNS("*", "X509Certificate").item(0);
      if (certificate.getNodeType() == Node.ELEMENT_NODE) {
        certificates.add(certificate.getTextContent());
      }
    }
    return certificates;
  }

  private Map<String, String> readSsoEndpoints(Element entityDescriptor) {
    Map<String, String> endpoints = new HashMap<>();
    NodeList services = entityDescriptor.getElementsByTagNameNS("*", "SingleSignOnService");
    for (int serviceIndex = 0; serviceIndex < services.getLength(); serviceIndex++) {
      Node serviceNode = services.item(serviceIndex);
      if (serviceNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }

      Element service = (Element) serviceNode;
      endpoints.put(service.getAttribute("Binding"), service.getAttribute("Location"));
    }
    return endpoints;
  }

  private void populateFriendlyName(IDP idp, Element entityDescriptor) {
    NodeList organizations = entityDescriptor.getElementsByTagNameNS("*", "Organization");
    if (organizations.getLength() == 0) {
      idp.setFriendlyName("CIE");
      return;
    }

    for (int organizationIndex = 0; organizationIndex < organizations.getLength();
        organizationIndex++) {
      Node organizationNode = organizations.item(organizationIndex);
      if (organizationNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }

      Element organization = (Element) organizationNode;
      NodeList names = organization.getElementsByTagNameNS("*", "OrganizationName");
      for (int nameIndex = 0; nameIndex < names.getLength(); nameIndex++) {
        Node nameNode = names.item(nameIndex);
        if (nameNode.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }

        Element name = (Element) nameNode;
        String language = name.getAttribute("xml:lang");
        if ("it".equals(language) || "en".equals(language)) {
          idp.setFriendlyName(name.getTextContent());
        }
      }
    }
  }

  private boolean isLatestSpidModify(JsonNode dynamodbEventRecord) {
    if (dynamodbEventRecord == null
        || !"MODIFY".equals(dynamodbEventRecord.path("eventName").asText())) {
      return false;
    }

    JsonNode newImage = dynamodbEventRecord.path("dynamodb").path("NewImage");
    return LatestTAG.LATEST_SPID.toString().equals(readStringAttribute(newImage, "pointer"));
  }

  private String readStringAttribute(JsonNode image, String attributeName) {
    JsonNode value = image.path(attributeName).path("S");
    if (value.isMissingNode() || value.isNull()) {
      return null;
    }

    return value.asText();
  }

  private Boolean readBooleanAttribute(JsonNode image, String attributeName) {
    JsonNode value = image.path(attributeName).path("BOOL");
    if (!value.isBoolean()) {
      return null;
    }

    return value.booleanValue();
  }

  private String serializePublicIdps(ArrayList<IDP> idps) {
    try {
      return objectMapper.writeValueAsString(idps);
    } catch (JsonProcessingException e) {
      Log.error("error serializing public IDPs snapshot " + ExceptionUtils.getStackTrace(e));
      throw new RuntimeException(e);
    }
  }
}
