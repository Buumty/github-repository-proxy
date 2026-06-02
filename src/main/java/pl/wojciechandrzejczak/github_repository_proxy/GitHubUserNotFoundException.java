package pl.wojciechandrzejczak.github_repository_proxy;

class GitHubUserNotFoundException extends RuntimeException{
    GitHubUserNotFoundException(String username) {
        super("Github user not found: " + username);
    }
}
