package pl.wojciechandrzejczak.github_repository_proxy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GitHubService {

    private final GitHubClient githubClient;

    GitHubService(GitHubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponse> listUserRepositories(String username) {
        return githubClient.listAllUserRepositories(username).stream()
                .filter(repository -> !repository.fork())
                .map(this::mapToRepositoryResponse)
                .toList();
    }

    private RepositoryResponse mapToRepositoryResponse(GitHubRepositoryResponse repository) {
        List<BranchResponse> branches = githubClient
                .getRepositoryBranches(repository.owner().login(), repository.name())
                .stream()
                .map(branch -> new BranchResponse(
                        branch.name(),
                        branch.commit().sha()
                ))
                .toList();

        return new RepositoryResponse(
                repository.name(),
                repository.owner().login(),
                branches
        );
    }
}