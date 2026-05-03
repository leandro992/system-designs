package systemdesigns.study.Bitly.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record LinkResponseDto(
		Long id,
		String originalUrl,
		String shortCode,
		String shortUrl,
		Long accessCount,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
