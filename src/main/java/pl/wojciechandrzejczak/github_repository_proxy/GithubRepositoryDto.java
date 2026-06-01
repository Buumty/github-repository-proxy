package pl.wojciechandrzejczak.github_repository_proxy;

public record GithubRepositoryDto(
        String name,
        boolean fork,
        GithubOwnerDto owner
) {
}
