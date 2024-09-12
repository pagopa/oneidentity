package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.common.model.enums.IDPStatus;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.model.enums.MetadataType;
import it.pagopa.oneid.connector.S3BucketIDPMetadataConnectorImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@ApplicationScoped
public class IDPMetadataServiceImpl implements IDPMetadataService {

  @Inject
  S3BucketIDPMetadataConnectorImpl s3BucketIDPMetadataConnector;

  @Inject
  IDPConnectorImpl idpConnectorImpl;

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

      NodeList nodeList = doc.getElementsByTagNameNS("*", "EntityDescriptor");

      for (int parameter = 0; parameter < nodeList.getLength(); parameter++) {
        Node nodeEntityDescriptor = nodeList.item(parameter);
        if (nodeEntityDescriptor.getNodeType() == Node.ELEMENT_NODE) {
          Element eElementEntityDescriptor = (Element) nodeEntityDescriptor;
          IDP idp = new IDP();
          idp.setTimestamp(idpS3FileDTO.getTimestamp());
          idp.setPointer(String.valueOf(idpS3FileDTO.getLatestTAG()));
          idp.setStatus(IDPStatus.OK);
          idp.setActive(true);
          // Save entityID
          String entityID = eElementEntityDescriptor.getAttribute("entityID");
          idp.setEntityID(entityID);
          NodeList nodeListIDPSSODescriptor = eElementEntityDescriptor.getElementsByTagNameNS("*",
              "IDPSSODescriptor");

          for (int i = 0; i < nodeListIDPSSODescriptor.getLength(); i++) {
            Node nodeIDPSSODescriptor = nodeList.item(parameter);
            if (nodeIDPSSODescriptor.getNodeType() == Node.ELEMENT_NODE) {
              Element eElementIDPSSODescriptor = (Element) nodeIDPSSODescriptor;

              // Get KeyDescriptor node list
              NodeList nodeListKeyDescriptor = eElementIDPSSODescriptor.getElementsByTagNameNS("*",
                  "KeyDescriptor");
              // Get SingleSignOnService node list
              NodeList nodeListSingleSignOnService = eElementIDPSSODescriptor.getElementsByTagNameNS(
                  "*",
                  "SingleSignOnService");

              Set<String> certificates = new HashSet<>();
              Map<String, String> idpSSOEndpoints = new HashMap<>();

              for (int j = 0; j < nodeListKeyDescriptor.getLength(); j++) {
                Node nodeKeyDescriptor = nodeListKeyDescriptor.item(j);
                if (nodeKeyDescriptor.getNodeType() == Node.ELEMENT_NODE) {
                  Element eElementKeyDescriptor = (Element) nodeKeyDescriptor;
                  String use = eElementKeyDescriptor.getAttribute("use");
                  // We only need certificates used for signing
                  if (use.equals("signing")) {
                    NodeList nodeListX509Certificate = eElementKeyDescriptor.getElementsByTagNameNS(
                        "*", "X509Certificate");
                    Node nodeX509Certificate = nodeListX509Certificate.item(0);
                    if (nodeX509Certificate.getNodeType() == Node.ELEMENT_NODE) {
                      Element eElementX509Certificate = (Element) nodeX509Certificate;
                      String certificate = eElementX509Certificate.getTextContent();
                      certificates.add(certificate);
                    }

                  }
                }
              }

              idp.setCertificates(certificates);

              for (int z = 0; z < nodeListSingleSignOnService.getLength(); z++) {
                Node nodeSingleSignOnService = nodeListSingleSignOnService.item(z);
                if (nodeSingleSignOnService.getNodeType() == Node.ELEMENT_NODE) {
                  Element eElementSingleSignOnService = (Element) nodeSingleSignOnService;
                  String binding = eElementSingleSignOnService.getAttribute("Binding");
                  String location = eElementSingleSignOnService.getAttribute("Location");
                  idpSSOEndpoints.put(binding, location);
                }

              }

              idp.setIdpSSOEndpoints(idpSSOEndpoints);


            }

          }
          idpList.add(idp);
        }
      }
    } catch (Exception e) {
      Log.error("error parsing IDP metadata");
    }

    return idpList;
  }

  @Override
  public void updateIDPMetadata(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO) {

    if (idpS3FileDTO.getMetadataType().equals(MetadataType.SPID)) {
      idpConnectorImpl.saveIDPs(idpMetadata, LatestTAG.LATEST_SPID);
    } else {
      idpConnectorImpl.saveIDPs(idpMetadata, LatestTAG.LATEST_CIE);
    }

  }

  @Override
  public String getMetadataFile(String fileName) {
    return s3BucketIDPMetadataConnector.getMetadataFile(fileName);
  }
}
