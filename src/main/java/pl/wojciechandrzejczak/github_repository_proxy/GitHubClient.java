package pl.wojciechandrzejczak.github_repository_proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
class GitHubClient {

    private final RestClient client;

    GitHubClient(@Value("${github.api.base-url}") String githubApiBaseUrl) {
        this.client = RestClient.builder()
                .baseUrl(githubApiBaseUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    List<GitHubRepositoryResponse> listAllUserRepositories(String username) {
        List<GitHubRepositoryResponse> repositories = client.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode().value() == 404) {
                        throw new GitHubUserNotFoundException(username);
                    }
                })
                .body(new ParameterizedTypeReference<>() {
                });

        if (repositories == null) {
            return List.of();
        }
        return repositories;
    }

    List<GitHubBranchResponse> getRepositoryBranches(String owner, String repositoryName) {
        List<GitHubBranchResponse> branches = client.get()
                .uri("/repos/{owner}/{repo}/branches", owner, repositoryName)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (branches == null) {
            return List.of();
        }

        return branches;
    }
}