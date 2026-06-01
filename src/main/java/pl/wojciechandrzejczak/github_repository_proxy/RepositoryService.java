package pl.wojciechandrzejczak.github_repository_proxy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class RepositoryService {

    private final GithubClient githubClient;

    RepositoryService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponseDto> listUserRepositories(String username) {
        return githubClient.listAllUserRepositories(username).stream()
                .filter(repository -> !repository.fork())
                .map(repository -> {
                    List<BranchResponseDto> branches = githubClient
                            .getRepositoryBranches(repository.owner().login(), repository.name())
                            .stream()
                            .map(branch -> new BranchResponseDto(
                                    branch.name(),
                                    branch.commit().sha()
                            ))
                            .toList();

                    return new RepositoryResponseDto(
                            repository.name(),
                            repository.owner().login(),
                            branches
                    );
                })
                .toList();
    }
}