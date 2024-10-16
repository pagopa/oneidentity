package it.pagopa.oneid.service;

public interface GitHubService {

  void openPullRequest(String title, String base, String branchName,
      String metadataContent, String metadataPath, String idpType);
}
