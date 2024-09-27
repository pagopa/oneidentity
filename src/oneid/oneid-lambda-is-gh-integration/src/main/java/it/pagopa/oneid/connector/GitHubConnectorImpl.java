package it.pagopa.oneid.connector;

import static it.pagopa.oneid.utils.Constants.GH_PERSONAL_ACCESS_TOKEN;
import static it.pagopa.oneid.utils.Constants.METADATA_BASE_PATH;
import static it.pagopa.oneid.utils.Constants.REPOSITORY_NAME;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@ApplicationScoped
public class GitHubConnectorImpl implements GitHubConnector {


  private final GHRepository repository;

  @Inject
  GitHubConnectorImpl(SsmClient ssmClient) {

    try {
      String ghPersonalAccessTokenValue = getParameterValue(ssmClient, GH_PERSONAL_ACCESS_TOKEN);
      GitHub github = new GitHubBuilder().withOAuthToken(ghPersonalAccessTokenValue).build();
      repository = github.getRepository(REPOSITORY_NAME);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public String getParameterValue(SsmClient ssmClient, String parameterName) {
    GetParameterRequest request = GetParameterRequest.builder()
        .name(parameterName)
        .withDecryption(true)  // Set to true if the parameter is encrypted
        .build();

    GetParameterResponse response = ssmClient.getParameter(request);
    return response.parameter().value();

  }

  @Override
  public void createBranchAndCommit(String branchName, String idpType, String fileContent,
      String metadataPath) {
    Log.debug("start");

    // create branch
    try {
      GHBranch mainBranch = repository.getBranch("main");

      repository.createRef("refs/heads/" + branchName, mainBranch.getSHA1());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    GHContent existingFile = findFileInDirectory(METADATA_BASE_PATH, idpType,
        branchName);

    if (existingFile != null) {
      // delete the existing file if it exists
      try {
        existingFile.delete("feat: remove metadata file for " + idpType, branchName);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // Create a new file with updated content
    createFileWithUpdatedContent(fileContent, metadataPath, branchName, idpType);
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public void createPullRequest(String title, String head, String base) {
    Log.debug("start");

    try {
      repository.createPullRequest(title, head, base, "");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
