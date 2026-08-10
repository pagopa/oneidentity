package it.pagopa.oneid.common.model;

import java.util.Set;

/** Identifies eIDAS clients reserved for SAML metadata. */
public final class EidasReservedClientPolicy {

  private static final Set<Integer> RESERVED_ACS_INDEXES = Set.of(99, 100);

  private EidasReservedClientPolicy() {
  }

  public static boolean isReserved(Client client) {
    return client != null && isReservedAcsIndex(client.getAcsIndex());
  }

  public static boolean isReservedAcsIndex(int acsIndex) {
    return RESERVED_ACS_INDEXES.contains(acsIndex);
  }
}