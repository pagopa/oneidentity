package it.pagopa.oneid.connector;

import static it.pagopa.oneid.utils.Constants.GH_PERSONAL_ACCESS_TOKEN;
import static it.pagopa.oneid.utils.Constants.REPOSITORY_NAME;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import java.io.IOException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@ApplicationScoped
public class RepositoryConfig {

  @ApplicationScoped
  @Produces
  GHRepository ghRepository(SsmClient ssmClient) {
    Log.debug("start");
    String ghPersonalAccessTokenValue = getParameterValue(ssmClient, GH_PERSONAL_ACCESS_TOKEN);
    GHRepository repository;
    try {
      GitHub github = new GitHubBuilder().withOAuthToken(ghPersonalAccessTokenValue).build();
      repository = github.getRepository(REPOSITORY_NAME);
    } catch (IOException e) {
      Log.error("error connecting to github repo: " + e.getMessage());
      throw new RuntimeException(e);
    }
    return repository;
  }


  private String getParameterValue(SsmClient ssmClient, String parameterName) {
    Log.debug("start");
    GetParameterResponse response;
    GetParameterRequest request = GetParameterRequest.builder()
        .name(parameterName)
        .withDecryption(true)  // Set to true if the parameter is encrypted
        .build();
    try {
      response = ssmClient.getParameter(request);
    } catch (AwsServiceException e) {
      Log.error("error retrieving parameter from parameter store: " + e.getMessage());
      throw new RuntimeException(e);
    }
    return response.parameter().value();

  }

}