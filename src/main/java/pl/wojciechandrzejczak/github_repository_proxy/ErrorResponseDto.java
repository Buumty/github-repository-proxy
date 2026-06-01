package pl.wojciechandrzejczak.github_repository_proxy;

record ErrorResponseDto(
        int status,
        String message
) {
}