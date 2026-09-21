package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.PairwiseMode;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Alternative
@Dependent
public class MockClientLookupService implements ClientLookupService {

  private static final Map<String, Client> CLIENTS = new HashMap<>();

  static {
    addClient("test", SamlBinding.HTTP_POST, true, null);
    addClient("testRedirect", SamlBinding.HTTP_REDIRECT, true, null);
    addClient("testEidasIndexMissing", SamlBinding.HTTP_POST, true, null, null);
    addClient("testIsRequiredSameIdpFalse", SamlBinding.HTTP_POST, false, null);
    addClient("testIsRequiredSameIdpTrue", SamlBinding.HTTP_POST, true, null);
    addClient("testPairwiseTrue", SamlBinding.HTTP_POST, false, PairwiseMode.TOKEN);
    addClient("testPairwisePDV", SamlBinding.HTTP_POST, false, PairwiseMode.PDV);
    addClient("testClientId", SamlBinding.HTTP_POST, true, null);
  }

  private static void addClient(String clientId, SamlBinding samlBinding, boolean requiredSameIdp,
      PairwiseMode pairwise) {
    addClient(clientId, samlBinding, requiredSameIdp, pairwise, 99);
  }

  private static void addClient(String clientId, SamlBinding samlBinding, boolean requiredSameIdp,
      PairwiseMode pairwise, Integer eidasIndex) {
    CLIENTS.put(clientId, Client.builder()
        .clientId(clientId)
        .userId("test")
        .friendlyName("test")
        .callbackURI(Set.of("foo.bar", "https://client.example.com/callback"))
        .requestedParameters(Set.of("test"))
        .authLevel(AuthLevel.L2)
        .samlBinding(samlBinding)
        .acsIndex(0)
        .attributeIndex(0)
        .eidasIndex(eidasIndex)
        .isActive(true)
        .clientIdIssuedAt(0)
        .logoUri("test")
        .policyUri("test")
        .tosUri("test")
        .requiredSameIdp(requiredSameIdp)
        .a11yUri("test")
        .backButtonEnabled(false)
        .pairwise(pairwise)
        .build());
  }

  @Override
  public Optional<Client> getClientById(String clientId) {
    return Optional.ofNullable(CLIENTS.get(clientId));
  }
}
