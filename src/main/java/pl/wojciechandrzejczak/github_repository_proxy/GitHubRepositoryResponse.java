package pl.wojciechandrzejczak.github_repository_proxy;

record GitHubRepositoryResponse(
        String name,
        boolean fork,
        GitHubOwnerResponse owner
) {}
record GitHubOwnerResponse(
        String login
){}

