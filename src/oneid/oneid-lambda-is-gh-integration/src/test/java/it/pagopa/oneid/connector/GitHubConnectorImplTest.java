package it.pagopa.oneid.connector;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHContentBuilder;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;

@QuarkusTest
public class GitHubConnectorImplTest {

  @InjectMock
  GHRepository repository;
  @Inject
  private GitHubConnectorImpl gitHubConnectorImpl;

  @SneakyThrows
  @BeforeEach
  public void beforeEach() {
    GHBranch mockBranch = mock(GHBranch.class);
    when(repository.getBranch("main")).thenReturn(mockBranch);

    GHRef mockRef = mock(GHRef.class);
    when(repository.createRef(anyString(), anyString())).thenReturn(mockRef);

    GHContentBuilder builder = mock(GHContentBuilder.class);
    when(builder.content(anyString())).thenReturn(builder);
    when(builder.message(anyString())).thenReturn(builder);
    when(builder.path(anyString())).thenReturn(builder);
    when(builder.branch(anyString())).thenReturn(builder);
    when(repository.createContent()).thenReturn(builder);
  }


  @Test
  @SneakyThrows
  void createBranchAndCommit_withNonExistingFile() {

    when(repository.getDirectoryContent(any(), any())).thenThrow(IOException.class);

    Executable executable = () -> gitHubConnectorImpl.createBranchAndCommit("test-branch",
        "spid", "content", "/test");

    Assertions.assertDoesNotThrow(executable);
  }

  @Test
  @SneakyThrows
  void createBranchAndCommit_withExistingFile() {

    List<GHContent> contentList = new ArrayList<>();
    GHContent content = mock(GHContent.class);
    when(content.getName()).thenReturn("spid");
    contentList.add(content);
    when(repository.getDirectoryContent(any(), any())).thenReturn(contentList);

    Executable executable = () -> gitHubConnectorImpl.createBranchAndCommit("test-branch",
        "spid", "content", "/test");
    Assertions.assertDoesNotThrow(executable);
  }

  @Test
  @SneakyThrows
  void createBranchAndCommit_errorDeletingExistingFile() {

    GHContent content = mock(GHContent.class);
    when(content.getName()).thenReturn("spid");

    List<GHContent> contentList = new ArrayList<>();
    contentList.add(content);
    when(content.delete(anyString(), anyString())).thenThrow(IOException.class);
    when(repository.getDirectoryContent(any(), any())).thenReturn(contentList);

    Executable executable = () -> gitHubConnectorImpl.createBranchAndCommit("test-branch",
        "spid", "content", "/test");

    Assertions.assertThrows(RuntimeException.class, executable);
  }

  @Test
  @SneakyThrows
  void createBranchAndCommit_errorCreatingCommit() {

    when(repository.getDirectoryContent(any(), any())).thenThrow(IOException.class);
    when(repository.createContent().commit()).thenThrow(IOException.class);

    Executable executable = () -> gitHubConnectorImpl.createBranchAndCommit("test-branch",
        "spid", "content", "/test");

    Assertions.assertThrows(RuntimeException.class, executable);
  }

  @Test
  @SneakyThrows
  void createBranchAndCommit_withoutMainBranch() {
    when(repository.getBranch("main")).thenReturn(null);

    Executable executable = () -> gitHubConnectorImpl.createBranchAndCommit("test-branch",
        "spid", "content", "/test");

    Assertions.assertThrows(RuntimeException.class, executable);
  }

  @Test
  @SneakyThrows
  void createBranchAndCommit_errorCreatingRef() {

    when(repository.getBranch(anyString())).thenThrow(IOException.class);
    Executable executable = () -> gitHubConnectorImpl.createBranchAndCommit("test-branch",
        "spid", "content", "/test");

    Assertions.assertThrows(RuntimeException.class, executable);
  }


  @Test
  void createPullRequest_OK() {

    Executable executable = () -> gitHubConnectorImpl.createPullRequest("title", "feat", "main");
    Assertions.assertDoesNotThrow(executable);
  }

  @SneakyThrows
  @Test
  void createPullRequest_error() {

    when(repository.createPullRequest("title", "feat", "main", "")).thenThrow(IOException.class);

    Executable executable = () -> gitHubConnectorImpl.createPullRequest("title", "feat", "main");
    Assertions.assertThrows(RuntimeException.class, executable);
  }
}