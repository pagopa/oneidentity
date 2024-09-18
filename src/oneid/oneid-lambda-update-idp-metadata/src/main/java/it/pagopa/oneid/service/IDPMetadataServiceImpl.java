package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.connector.IDPConnectorImpl;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.dto.IdpS3FileDTO;
import it.pagopa.oneid.common.model.enums.IDPStatus;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
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

      //region EntityDescriptor
      NodeList nodeList = doc.getElementsByTagNameNS("*", "EntityDescriptor");

      // For each EntityDescriptor
      for (int parameter = 0; parameter < nodeList.getLength(); parameter++) {

        Node nodeEntityDescriptor = nodeList.item(parameter);
        if (nodeEntityDescriptor.getNodeType() == Node.ELEMENT_NODE) {
          Element eElementEntityDescriptor = (Element) nodeEntityDescriptor;

          // Initialize IDP fields
          IDP idp = new IDP();
          idp.setTimestamp(idpS3FileDTO.getTimestamp());
          idp.setPointer(String.valueOf(idpS3FileDTO.getLatestTAG()));
          idp.setStatus(IDPStatus.OK);
          idp.setActive(true);

          // Get entityID from file
          String entityID = eElementEntityDescriptor.getAttribute("entityID");
          idp.setEntityID(entityID);

          //region IDPSSODescriptor
          NodeList nodeListIDPSSODescriptor = eElementEntityDescriptor.getElementsByTagNameNS("*",
              "IDPSSODescriptor");

          // For each node of IDPSSODescriptor
          for (int i = 0; i < nodeListIDPSSODescriptor.getLength(); i++) {

            Node nodeIDPSSODescriptor = nodeList.item(parameter);
            if (nodeIDPSSODescriptor.getNodeType() == Node.ELEMENT_NODE) {
              Element eElementIDPSSODescriptor = (Element) nodeIDPSSODescriptor;

              // Get KeyDescriptor node list
              NodeList nodeListKeyDescriptor = eElementIDPSSODescriptor.getElementsByTagNameNS("*",
                  "KeyDescriptor");

              //region Certificates
              Set<String> certificates = new HashSet<>();

              // For each node of KeyDescriptor
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
              //endregion

              //region IdpSSOEndpoints

              // Get SingleSignOnService node list
              NodeList nodeListSingleSignOnService = eElementIDPSSODescriptor.getElementsByTagNameNS(
                  "*", "SingleSignOnService");
              Map<String, String> idpSSOEndpoints = new HashMap<>();

              // For each node of SingleSignOnService
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
              //endregion

            }
          }
          //endregion

          //region Organization - FriendlyName

          // Get Organization node list
          NodeList nodeListOrganization = eElementEntityDescriptor.getElementsByTagNameNS("*",
              "Organization");

          // SPID - IdP
          if (nodeListOrganization.getLength() != 0) {
            // For each node of Organization
            for (int w = 0; w < nodeListOrganization.getLength(); w++) {

              Node nodeOrganization = nodeListOrganization.item(w);
              if (nodeOrganization.getNodeType() == Node.ELEMENT_NODE) {
                Element eElementOrganization = (Element) nodeOrganization;

                // Get OrganizationDisplayName node list
                NodeList nodeListOrganizationName = eElementOrganization.getElementsByTagNameNS(
                    "*", "OrganizationName");

                for (int x = 0; x < nodeListOrganizationName.getLength(); x++) {

                  Node nodeOrganizationName = nodeListOrganizationName.item(x);
                  if (nodeOrganizationName.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElementOrganizationName = (Element) nodeOrganizationName;

                    // Italian organization name or, if missing, English name is fine
                    String xmlLang = eElementOrganizationName.getAttribute("xml:lang");
                    if (xmlLang.equals("it") || xmlLang.equals("en")) {
                      String organizationName = eElementOrganizationName.getTextContent();
                      idp.setFriendlyName(organizationName);
                    }
                  }
                }
              }
            }
          }// CiE - IdP
          else {
            idp.setFriendlyName("CIE");
          }
          //endregion

          idpList.add(idp);
        }
      }
      //endregion

    } catch (Exception e) {
      Log.error("error parsing IDP metadata " + ExceptionUtils.getStackTrace(e));
    }

    return idpList;
  }

  @Override
  public void updateIDPMetadata(ArrayList<IDP> idpMetadata, IdpS3FileDTO idpS3FileDTO) {
    idpConnectorImpl.saveIDPs(idpMetadata, idpS3FileDTO.getLatestTAG());
  }

  @Override
  public String getMetadataFile(String fileName) {
    return s3BucketIDPMetadataConnector.getMetadataFile(fileName);
  }
}
