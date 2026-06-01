package pl.wojciechandrzejczak.github_repository_proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
class GithubClient {

    private final RestClient client;

    GithubClient(@Value("${github.api.base-url}") String githubApiBaseUrl) {
        this.client = RestClient.builder()
                .baseUrl(githubApiBaseUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    List<GithubRepositoryDto> listAllUserRepositories(String username) {
        GithubRepositoryDto[] repositories = client.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode().value() == 404) {
                        throw new GithubUserNotFoundException(username);
                    }
                })
                .body(GithubRepositoryDto[].class);

        return repositories == null ? List.of() : Arrays.asList(repositories);
    }

    List<GithubBranchDto> getRepositoryBranches(String owner, String repositoryName) {
        GithubBranchDto[] branches = client.get()
                .uri("/repos/{owner}/{repo}/branches", owner, repositoryName)
                .retrieve()
                .body(GithubBranchDto[].class);

        return branches == null ? List.of() : Arrays.asList(branches);
    }
}