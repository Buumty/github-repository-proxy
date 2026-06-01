package pl.wojciechandrzejczak.github_repository_proxy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class RepositoryController {

    private final RepositoryService repositoryService;

    RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping("/api/users/{username}/repositories")
    List<RepositoryResponseDto> listUserRepositories(@PathVariable String username) {
        return repositoryService.listUserRepositories(username);
    }
}