package pl.wojciechandrzejczak.github_repository_proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubRepositoryProxyApplicationTests {

	private static final WireMockServer wireMockServer =
			new WireMockServer(options().dynamicPort());

	static {
		wireMockServer.start();
	}

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("github.api.base-url", wireMockServer::baseUrl);
	}

	@BeforeEach
	void setUp() {
		wireMockServer.resetAll();
	}

	@AfterAll
	static void tearDown() {
		wireMockServer.stop();
	}

	@Test
	void givenRepositoriesWithForks_whenListingUserRepositories_thenReturnOnlyNonForkRepositoriesWithBranches() throws Exception {
		// given
		wireMockServer.stubFor(get(urlEqualTo("/users/testuser/repos"))
				.willReturn(okJson("""
                        [
                          {
                            "name": "repo-a",
                            "fork": false,
                            "owner": {
                              "login": "testuser"
                            }
                          },
                          {
                            "name": "repo-b",
                            "fork": true,
                            "owner": {
                              "login": "testuser"
                            }
                          }
                        ]
                        """)));

		wireMockServer.stubFor(get(urlEqualTo("/repos/testuser/repo-a/branches"))
				.willReturn(okJson("""
                        [
                          {
                            "name": "main",
                            "commit": {
                              "sha": "abc123"
                            }
                          },
                          {
                            "name": "develop",
                            "commit": {
                              "sha": "def456"
                            }
                          }
                        ]
                        """)));

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/api/users/testuser/repositories"))
				.GET()
				.build();

		// when
		HttpResponse<String> response = httpClient.send(
				request,
				HttpResponse.BodyHandlers.ofString()
		);

		// then
		assertThat(response.statusCode()).isEqualTo(200);

		JsonNode body = objectMapper.readTree(response.body());

		assertThat(body).hasSize(1);

		JsonNode repository = body.get(0);
		assertThat(repository.get("repositoryName").asString()).isEqualTo("repo-a");
		assertThat(repository.get("ownerLogin").asString()).isEqualTo("testuser");

		JsonNode branches = repository.get("branches");
		assertThat(branches).hasSize(2);

		JsonNode mainBranch = branches.get(0);
		assertThat(mainBranch.get("name").asString()).isEqualTo("main");
		assertThat(mainBranch.get("lastCommitSha").asString()).isEqualTo("abc123");

		JsonNode developBranch = branches.get(1);
		assertThat(developBranch.get("name").asString()).isEqualTo("develop");
		assertThat(developBranch.get("lastCommitSha").asString()).isEqualTo("def456");

		wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/testuser/repos")));
		wireMockServer.verify(1, getRequestedFor(urlEqualTo("/repos/testuser/repo-a/branches")));
		wireMockServer.verify(0, getRequestedFor(urlEqualTo("/repos/testuser/repo-b/branches")));
	}

	@Test
	void givenNotExistingGithubUser_whenListingUserRepositories_thenReturn404WithErrorResponse() throws Exception {
		// given
		wireMockServer.stubFor(get(urlEqualTo("/users/unknown/repos"))
				.willReturn(notFound()));

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/api/users/unknown/repositories"))
				.GET()
				.build();

		// when
		HttpResponse<String> response = httpClient.send(
				request,
				HttpResponse.BodyHandlers.ofString()
		);

		// then
		assertThat(response.statusCode()).isEqualTo(404);
		JsonNode body = objectMapper.readTree(response.body());

		assertThat(body.get("status").asInt()).isEqualTo(404);
		assertThat(body.get("message").asString()).isEqualTo("GitHub user not found: unknown");

		wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/unknown/repos")));
	}
}