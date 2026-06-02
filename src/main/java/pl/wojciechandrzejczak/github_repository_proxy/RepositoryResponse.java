package pl.wojciechandrzejczak.github_repository_proxy;

import java.util.List;

record RepositoryResponse(
        String repositoryName,
        String ownerLogin,
        List<BranchResponse> branches
) {
}

record BranchResponse(
        String name,
        String lastCommitSha
) {}
