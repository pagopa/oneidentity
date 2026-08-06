package it.pagopa.oneid.common.model;

import java.util.Set;

/** Identifies active technical eIDAS clients that are reserved for SAML metadata. */
public final class EidasTechnicalClientPolicy {

  private static final Set<Integer> TECHNICAL_ACS_INDEXES = Set.of(99, 100);

  private EidasTechnicalClientPolicy() {
  }

  public static boolean isTechnical(Client client) {
    return client != null && isTechnicalAcsIndex(client.getAcsIndex());
  }

  public static boolean isTechnicalAcsIndex(int acsIndex) {
    return TECHNICAL_ACS_INDEXES.contains(acsIndex);
  }
}
