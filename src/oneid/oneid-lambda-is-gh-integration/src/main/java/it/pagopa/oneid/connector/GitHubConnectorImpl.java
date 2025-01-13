package it.pagopa.oneid.connector;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.utils.logging.CustomLogging;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

@ApplicationScoped
@CustomLogging
public class GitHubConnectorImpl implements GitHubConnector {

  @Inject
  GHRepository repository;

  @Override
  public void createBranchAndCommit(String branchName, String idpType, String fileContent,
      String metadataPath) {

    // create branch
    try {
      GHBranch mainBranch = repository.getBranch("main");
      repository.createRef("refs/heads/" + branchName, mainBranch.getSHA1());
    } catch (IOException | NullPointerException e) {
      Log.error("error creating a branch : " + e.getMessage());
      throw new RuntimeException(e);
    }

    // Create or update metadata file
    createOrUpdateFile(fileContent, metadataPath, branchName, idpType);
    Log.debug("successfully created/updated metadata file");

  }


  private GHContent findFileInDirectory(String metadataDirPath, String idpType, String branchName) {
    try {
      return repository.getDirectoryContent(metadataDirPath, branchName)
          .stream()
          .filter(file -> file.getName().contains(idpType))
          .findFirst()
          .orElse(null);
    } catch (IOException e) {
      // file does not exist
      Log.debug("file does not exist, needs to be created");
      return null;
    }
  }

  private void createOrUpdateFile(String fileContent, String metadataPath,
      String branchName,
      String idpType) {
    try {
      repository.createContent()
          .content(fileContent)
          .message("feat: update metadata file for " + idpType)
          .path(metadataPath)
          .branch(branchName)
          .commit();
      Log.debug("created/updated file metadata for " + idpType);
    } catch (IOException e) {
      Log.error("error creating commit with new metadata: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }


  @Override
  public void createPullRequest(String title, String head, String base) {

    try {
      repository.createPullRequest(title, head, base, "");
    } catch (IOException e) {
      Log.error("error creating new pull request: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

}
