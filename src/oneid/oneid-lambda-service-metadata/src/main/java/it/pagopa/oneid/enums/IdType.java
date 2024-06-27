package it.pagopa.oneid.enums;

import it.pagopa.oneid.common.utils.SAMLUtilsConstants;

public enum IdType {
  spid(SAMLUtilsConstants.NAMESPACE_PREFIX_SPID, SAMLUtilsConstants.NAMESPACE_URI_SPID),
  cie(SAMLUtilsConstants.NAMESPACE_PREFIX_CIE, SAMLUtilsConstants.NAMESPACE_URI_CIE);

  private final String namespacePrefix;
  private final String namespaceUri;


  IdType(String namespacePrefix, String namespaceUri) {
    this.namespacePrefix = namespacePrefix;
    this.namespaceUri = namespaceUri;
  }

  public String getNamespacePrefix() {
    return namespacePrefix;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

}
