package it.pagopa.oneid.connector;

import static it.pagopa.oneid.utils.Constants.METADATA_BASE_PATH;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

@ApplicationScoped
public class GitHubConnectorImpl implements GitHubConnector {

  @Inject
  GHRepository repository;

  @Override
  public void createBranchAndCommit(String branchName, String idpType, String fileContent,
      String metadataPath) {
    Log.debug("start");

    // create branch
    try {
      GHBranch mainBranch = repository.getBranch("main");
      repository.createRef("refs/heads/" + branchName, mainBranch.getSHA1());
    } catch (IOException | NullPointerException e) {
      Log.error("error creating a branch : " + e.getMessage());
      throw new RuntimeException(e);
    }

    GHContent existingFile = findFileInDirectory(METADATA_BASE_PATH, idpType,
        branchName);

    if (existingFile != null) {
      // delete the existing file if it exists
      try {
        existingFile.delete("feat: remove metadata file for " + idpType, branchName);
      } catch (IOException e) {
        Log.error("error deleting existing metadata file: " + e.getMessage());
        throw new RuntimeException(e);
      }
      Log.debug("deleted metadata file for " + idpType);
    }

    // Create a new file with updated content
    createFileWithUpdatedContent(fileContent, metadataPath, branchName, idpType);
    Log.debug("successfully created new metadata file");
  }


  private GHContent findFileInDirectory(String metadataDirPath, String idpType, String branchName) {
    Log.debug("start");
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

  private void createFileWithUpdatedContent(String fileContent, String metadataPath,
      String branchName,
      String idpType) {
    Log.debug("start");
    try {
      repository.createContent()
          .content(fileContent)
          .message("feat: update metadata file for " + idpType)
          .path(metadataPath)
          .branch(branchName)
          .commit();
      Log.debug("created file metadata for " + idpType);
    } catch (IOException e) {
      Log.error("error creating commit with new metadata: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }


  @Override
  public void createPullRequest(String title, String head, String base) {
    Log.debug("start");

    try {
      repository.createPullRequest(title, head, base, "");
    } catch (IOException e) {
      Log.error("error creating new pull request: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

}
