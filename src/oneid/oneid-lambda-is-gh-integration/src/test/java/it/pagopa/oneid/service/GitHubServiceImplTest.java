package it.pagopa.oneid.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.connector.GitHubConnectorImpl;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

@QuarkusTest
class GitHubServiceImplTest {

  @Inject
  GitHubServiceImpl gitHubServiceImpl;

  @InjectMock
  GitHubConnectorImpl gitHubConnectorImpl;

  @Test
  void openPullRequest() {
    Executable executable = () -> gitHubServiceImpl.openPullRequest("title", "base", "branchName",
        "metadataContent", "metadataPath", "idpType");
    Assertions.assertDoesNotThrow(executable);

  }
}