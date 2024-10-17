package it.pagopa.oneid.service;

import it.pagopa.oneid.common.utils.logging.CustomLogging;
import it.pagopa.oneid.connector.GitHubConnectorImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@CustomLogging
public class GitHubServiceImpl implements GitHubService {

  @Inject
  GitHubConnectorImpl gitHubConnectorImpl;

  @Override
  public void openPullRequest(String title, String base, String branchName,
      String metadataContent, String metadataPath, String idpType) {

    //1. open branch and create commit
    gitHubConnectorImpl.createBranchAndCommit(branchName, idpType, metadataContent,
        metadataPath);
    //2. create PR
    gitHubConnectorImpl.createPullRequest(title, branchName, base);

  }

}
