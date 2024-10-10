package it.pagopa.oneid.service;

import io.quarkus.logging.Log;
import it.pagopa.oneid.connector.GitHubConnectorImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GitHubServiceImpl implements GitHubService {

  @Inject
  GitHubConnectorImpl gitHubConnectorImpl;

  @Override
  public void openPullRequest(String title, String base, String branchName,
      String metadataContent, String metadataPath, String idpType) {
    Log.debug("start");

    //1. open branch and create commit
    gitHubConnectorImpl.createBranchAndCommit(branchName, idpType, metadataContent,
        metadataPath);
    //2. create PR
    gitHubConnectorImpl.createPullRequest(title, branchName, base);

  }

}
