package systemdesigns.study.Bitly.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import systemdesigns.study.Bitly.entity.Link;
import systemdesigns.study.Bitly.repository.LinkRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LinkControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LinkRepository linkRepository;

    @BeforeEach
    void setUp() {
        linkRepository.deleteAll();
    }

    @Test
    void shouldCreateShortLinkAndPersistIt() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl": "https://example.com/page"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/page"))
                .andExpect(jsonPath("$.shortCode").isString())
                .andExpect(jsonPath("$.shortUrl").value(org.hamcrest.Matchers.startsWith("http://localhost:9090/")))
                .andExpect(jsonPath("$.accessCount").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        assertThat(linkRepository.count()).isEqualTo(1);

        Link savedLink = linkRepository.findAll().getFirst();
        assertThat(savedLink.getOriginalUrl()).isEqualTo("https://example.com/page");
        assertThat(savedLink.getShortCode()).hasSize(8);
        assertThat(savedLink.getAccessCount()).isZero();
        assertThat(savedLink.getCreatedAt()).isNotNull();
        assertThat(savedLink.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldReturnBadRequestWhenOriginalUrlIsInvalid() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl": "ftp://example.com/page"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Original URL must start with http:// or https://"))
                .andExpect(jsonPath("$.path").value("/api/links"));

        assertThat(linkRepository.count()).isZero();
    }
}
