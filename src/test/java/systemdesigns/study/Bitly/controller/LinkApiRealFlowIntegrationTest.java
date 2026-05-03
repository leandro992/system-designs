package systemdesigns.study.Bitly.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;
import systemdesigns.study.Bitly.dto.LinkRequestDto;
import systemdesigns.study.Bitly.dto.LinkResponseDto;
import systemdesigns.study.Bitly.entity.Link;
import systemdesigns.study.Bitly.exception.ApiErrorResponse;
import systemdesigns.study.Bitly.repository.LinkRepository;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LinkApiRealFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LinkRepository linkRepository;

    private HttpClient httpClient;


    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        linkRepository.deleteAll();
    }

    @Test
    void shouldCreateShortLinkAndPersistIt() throws Exception {
        LinkRequestDto requestBody = new LinkRequestDto("https://example.com/page");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(201);

        LinkResponseDto responseBody = objectMapper.readValue(response.body(), LinkResponseDto.class);

        assertThat(responseBody.id()).isNotNull();
        assertThat(responseBody.originalUrl()).isEqualTo("https://example.com/page");
        assertThat(responseBody.shortCode()).hasSize(8);
        assertThat(responseBody.shortUrl()).isEqualTo("http://localhost/" + responseBody.shortCode());
        assertThat(responseBody.accessCount()).isZero();
        assertThat(responseBody.createdAt()).isNotNull();
        assertThat(responseBody.updatedAt()).isNotNull();

        assertThat(linkRepository.count()).isEqualTo(1);

        Link savedLink = linkRepository.findAll().getFirst();
        assertThat(savedLink.getId()).isEqualTo(responseBody.id());
        assertThat(savedLink.getOriginalUrl()).isEqualTo("https://example.com/page");
        assertThat(savedLink.getShortCode()).isEqualTo(responseBody.shortCode());
        assertThat(savedLink.getAccessCount()).isZero();
        assertThat(savedLink.getCreatedAt()).isNotNull();
        assertThat(savedLink.getUpdatedAt()).isNotNull();
    }


    @Test
    void shouldReturnBadRequestWhenOriginalUrlIsInvalid() throws Exception {
        LinkRequestDto requestBody = new LinkRequestDto("ftp://example.com/page");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);

        ApiErrorResponse errorResponse = objectMapper.readValue(response.body(), ApiErrorResponse.class);

        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.error()).isEqualTo("Bad Request");
        assertThat(errorResponse.message()).isEqualTo("Original URL must start with http:// or https://");
        assertThat(errorResponse.path()).isEqualTo("/api/links");

        assertThat(linkRepository.count()).isZero();
    }

    @Test
    void shouldReturnBadRequestWhenOriginalUrlIsBlank() throws Exception {
        LinkRequestDto requestBody = new LinkRequestDto("   ");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);

        ApiErrorResponse errorResponse = objectMapper.readValue(response.body(), ApiErrorResponse.class);

        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.error()).isEqualTo("Bad Request");
        assertThat(errorResponse.message()).isEqualTo("Original URL cannot be null or blank.");
        assertThat(errorResponse.path()).isEqualTo("/api/links");

        assertThat(linkRepository.count()).isZero();
    }

    @Test
    void shouldCreateMultipleLinksWithDifferentShortCodes() throws Exception {
        Set<String> generatedCodes = new HashSet<>();

        for (int i = 1; i <= 3; i++) {
            LinkRequestDto requestBody = new LinkRequestDto("https://example.com/page-" + i);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(buildUri())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(201);

            LinkResponseDto responseBody = objectMapper.readValue(response.body(), LinkResponseDto.class);
            generatedCodes.add(responseBody.shortCode());
        }

        assertThat(linkRepository.count()).isEqualTo(3);
        assertThat(generatedCodes).hasSize(3);
    }

    private URI buildUri() {
        return URI.create("http://localhost:" + port + "/api/links");
    }


}
