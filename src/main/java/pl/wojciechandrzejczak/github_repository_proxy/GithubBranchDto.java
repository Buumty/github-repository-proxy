package pl.wojciechandrzejczak.github_repository_proxy;

public record GithubBranchDto(
        String name,
        GithubCommitDto commit
) {
}
