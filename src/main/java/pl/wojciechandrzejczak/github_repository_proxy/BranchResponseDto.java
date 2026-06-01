package pl.wojciechandrzejczak.github_repository_proxy;

public record BranchResponseDto(
        String name,
        String lastCommitSha
) {
}
