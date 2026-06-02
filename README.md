# GitHub Repository Proxy

A simple REST API proxy application built with Java 25 and Spring Boot 4.

The application retrieves GitHub repositories for a given user and returns only repositories that are not forks. For each repository, it also returns branch names and the latest commit SHA for every branch.

## Technologies

- Java 25
- Spring Boot 4
- Gradle Kotlin DSL
- JUnit 5
- WireMock

## Features

- Lists all non-fork GitHub repositories for a given user
- Returns repository name and owner login
- Returns branch name and latest commit SHA for each branch
- Returns a custom 404 response when the GitHub user does not exist
- Uses integration tests with WireMock

## API

### List user repositories

```http
GET /api/users/{username}/repositories
```

Example:

```http
GET http://localhost:8080/api/users/octocat/repositories
```

Example successful response:

```json
[
  {
    "repositoryName": "example-repository",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "abc123"
      }
    ]
  }
]
```

### Error response

If the GitHub user does not exist, the application returns:

```http
HTTP/1.1 404 Not Found
```

Example response:

```json
{
  "status": 404,
  "message": "GitHub user not found: unknown-user"
}
```

## How to run

Clone the repository:

```bash
git clone https://github.com/Buumty/github-repository-proxy.git
cd github-repository-proxy
```

Run the application:

```bash
./gradlew bootRun
```

The application starts on:

```text
http://localhost:8080
```

## How to test

Run tests:

```bash
./gradlew test
```

The tests are integration tests. WireMock is used to emulate the GitHub API.

## Configuration

The GitHub API base URL is configured in `application.properties`:

```properties
github.api.base-url=https://api.github.com
```

During integration tests, this value is replaced with a WireMock server URL.
