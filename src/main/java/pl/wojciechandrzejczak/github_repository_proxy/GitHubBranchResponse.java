package pl.wojciechandrzejczak.github_repository_proxy;

record GitHubBranchResponse(
        String name,
        GitHubCommitResponse commit
) {
}

record GitHubCommitResponse(
        String sha
){}
