package it.pagopa.oneid.service;

import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.SamlBinding;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.Produces;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Alternative
@Dependent
public class MockClientProducer {

  @ApplicationScoped
  @Produces
  public Map<String, Client> clientsMap() {
    Map<String, Client> map = new HashMap<>();
    ArrayList<Client> clients = new ArrayList<>();
    clients.add(
        Client.builder()
            .clientId("test")
            .userId("test")
            .friendlyName("test")
            .callbackURI(Set.of("foo.bar"))
            .requestedParameters(Set.of("test"))
            .authLevel(AuthLevel.L2)
            .samlBinding(SamlBinding.HTTP_POST)
            .acsIndex(0)
            .attributeIndex(0)
            .eidasIndex(99)
            .isActive(true)
            .clientIdIssuedAt(0)
            .logoUri("test")
            .policyUri("test")
            .tosUri("test")
            .requiredSameIdp(true)
            .a11yUri("test")
            .backButtonEnabled(false)
            .build());
    clients.add(
        Client.builder()
            .clientId("testRedirect")
            .userId("test")
            .friendlyName("test")
            .callbackURI(Set.of("foo.bar"))
            .requestedParameters(Set.of("test"))
            .authLevel(AuthLevel.L2)
            .samlBinding(SamlBinding.HTTP_REDIRECT)
            .acsIndex(0)
            .attributeIndex(0)
            .isActive(true)
            .clientIdIssuedAt(0)
            .logoUri("test")
            .policyUri("test")
            .tosUri("test")
            .requiredSameIdp(true)
            .a11yUri("test")
            .backButtonEnabled(false)
            .build());
    clients.add(
        Client.builder()
            .clientId("testIsRequiredSameIdpFalse")
            .userId("test")
            .friendlyName("test")
            .callbackURI(Set.of("foo.bar"))
            .requestedParameters(Set.of("test"))
            .authLevel(AuthLevel.L2)
            .samlBinding(SamlBinding.HTTP_POST)
            .acsIndex(0)
            .attributeIndex(0)
            .isActive(true)
            .clientIdIssuedAt(0)
            .logoUri("test")
            .policyUri("test")
            .tosUri("test")
            .requiredSameIdp(false)
            .a11yUri("test")
            .backButtonEnabled(false)
            .build());
    clients.add(
        Client.builder()
            .clientId("testIsRequiredSameIdpTrue")
            .userId("test")
            .friendlyName("test")
            .callbackURI(Set.of("foo.bar"))
            .requestedParameters(Set.of("test"))
            .authLevel(AuthLevel.L2)
            .samlBinding(SamlBinding.HTTP_POST)
            .acsIndex(0)
            .attributeIndex(0)
            .isActive(true)
            .clientIdIssuedAt(0)
            .logoUri("test")
            .policyUri("test")
            .tosUri("test")
            .requiredSameIdp(true)
            .a11yUri("test")
            .backButtonEnabled(false)
            .build());
    clients.add(
        Client.builder()
            .clientId("testPairwiseTrue")
            .userId("test")
            .friendlyName("test")
            .callbackURI(Set.of("foo.bar"))
            .requestedParameters(Set.of("test"))
            .authLevel(AuthLevel.L2)
            .samlBinding(SamlBinding.HTTP_POST)
            .acsIndex(0)
            .attributeIndex(0)
            .isActive(true)
            .clientIdIssuedAt(0)
            .logoUri("test")
            .policyUri("test")
            .tosUri("test")
            .requiredSameIdp(false)
            .a11yUri("test")
            .backButtonEnabled(false)
            .pairwise(true)
            .build());
    clients.add(
        Client.builder()
            .clientId("eidasReferenceClient")
            .userId("test")
            .friendlyName("eidasReferenceClient")
            .callbackURI(Set.of("foo.bar"))
            .requestedParameters(Set.of("test"))
            .authLevel(AuthLevel.L2)
            .samlBinding(SamlBinding.HTTP_POST)
            .acsIndex(0)
            .attributeIndex(0)
            .eidasIndex(99)
            .isActive(true)
            .clientIdIssuedAt(0)
            .logoUri("test")
            .policyUri("test")
            .tosUri("test")
            .requiredSameIdp(true)
            .a11yUri("test")
            .backButtonEnabled(false)
            .build());
    clients.forEach(client -> map.put(client.getClientId(), client));

    return map;
  }
}
