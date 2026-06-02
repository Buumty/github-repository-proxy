package pl.wojciechandrzejczak.github_repository_proxy;

record ErrorResponse(
        int status,
        String message
) {
}