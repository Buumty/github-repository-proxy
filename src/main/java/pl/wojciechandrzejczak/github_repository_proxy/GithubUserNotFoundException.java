package pl.wojciechandrzejczak.github_repository_proxy;

public class GithubUserNotFoundException extends RuntimeException{
    GithubUserNotFoundException(String username) {
        super("Github user not found: " + username);
    }
}
