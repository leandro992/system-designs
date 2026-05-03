package systemdesigns.study.Bitly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import systemdesigns.study.Bitly.dto.LinkRequestDto;
import systemdesigns.study.Bitly.dto.LinkResponseDto;
import systemdesigns.study.Bitly.entity.Link;
import systemdesigns.study.Bitly.repository.LinkRepository;

@Service
@RequiredArgsConstructor
public class LinkService {

    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private final LinkRepository linkRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;


    public LinkResponseDto createLink(LinkRequestDto request){
        validateBusinessRules(request);

        String shortCode = generateUniqueShortCode();

        Link link = Link.builder()
                .originalUrl(request.originalUrl())
                .shortCode(shortCode)
                .accessCount(0L)
                .build();

        Link savedLink = linkRepository.save(link);

        return LinkResponseDto.builder()
                .id(savedLink.getId())
                .originalUrl(savedLink.getOriginalUrl())
                .shortCode(savedLink.getShortCode())
                .shortUrl(buildShortUrl(savedLink.getShortCode()))
                .accessCount(savedLink.getAccessCount())
                .createdAt(savedLink.getCreatedAt())
                .updatedAt(savedLink.getUpdatedAt())
                .build();
    }


    private String generateUniqueShortCode() {
        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
            String generatedCode = shortCodeGenerator.generate();

            boolean alreadyExists = linkRepository.existsByShortCode(generatedCode);
            if (!alreadyExists) {
                return generatedCode;
            }
        }

        throw new IllegalStateException(
                "Could not generate a unique short code after " + MAX_GENERATION_ATTEMPTS + " attempts."
        );
    }

    private void validateBusinessRules(LinkRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }

        String originalUrl = request.originalUrl();
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException("Original URL cannot be null or blank.");
        }

        if (!(originalUrl.startsWith("http://") || originalUrl.startsWith("https://"))) {
            throw new IllegalArgumentException("Original URL must start with http:// or https://");
        }
    }

    private String buildShortUrl(String shortCode) {
        return baseUrl.endsWith("/")
                ? baseUrl + shortCode
                : baseUrl + "/" + shortCode;
    }

}
