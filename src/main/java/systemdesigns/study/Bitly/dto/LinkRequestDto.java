package systemdesigns.study.Bitly.dto;

import lombok.Builder;

@Builder
public record LinkRequestDto(String originalUrl) {
}
