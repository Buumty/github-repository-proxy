package pl.wojciechandrzejczak.github_repository_proxy;

public class GitHubUserNotFoundException extends RuntimeException{
    GitHubUserNotFoundException(String username) {
        super("Github user not found: " + username);
    }
}
