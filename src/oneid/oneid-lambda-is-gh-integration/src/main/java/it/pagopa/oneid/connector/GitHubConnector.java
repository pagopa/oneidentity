package it.pagopa.oneid.connector;

public interface GitHubConnector {


  void createBranchAndCommit(String branchName, String idpType,
      String fileContent, String metadataPath);

  void createPullRequest(String title, String head, String base);
}
