package pl.wojciechandrzejczak.github_repository_proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubRepositoryProxyApplicationTests {

	private static final WireMockServer wireMockServer =
			new WireMockServer(options().dynamicPort());

	static {
		wireMockServer.start();
	}

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

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
	void shouldReturnOnlyNonForkRepositoriesWithBranches() throws Exception {
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

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(200);

		assertThat(response.body()).contains("\"repositoryName\":\"repo-a\"");
		assertThat(response.body()).contains("\"ownerLogin\":\"testuser\"");
		assertThat(response.body()).contains("\"name\":\"main\"");
		assertThat(response.body()).contains("\"lastCommitSha\":\"abc123\"");
		assertThat(response.body()).contains("\"name\":\"develop\"");
		assertThat(response.body()).contains("\"lastCommitSha\":\"def456\"");

		assertThat(response.body()).doesNotContain("\"repositoryName\":\"repo-b\"");

		wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/testuser/repos")));
		wireMockServer.verify(1, getRequestedFor(urlEqualTo("/repos/testuser/repo-a/branches")));
		wireMockServer.verify(0, getRequestedFor(urlEqualTo("/repos/testuser/repo-b/branches")));
	}

	@Test
	void shouldReturn404WhenGithubUserDoesNotExist() throws Exception {
		wireMockServer.stubFor(get(urlEqualTo("/users/unknown/repos"))
				.willReturn(notFound()));

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/api/users/unknown/repositories"))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(404);
		assertThat(response.body()).contains("\"status\":404");
		assertThat(response.body()).contains("\"message\":\"Github user not found: unknown\"");

		wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/unknown/repos")));
	}
}