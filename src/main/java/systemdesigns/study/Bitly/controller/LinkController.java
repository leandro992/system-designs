package systemdesigns.study.Bitly.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import systemdesigns.study.Bitly.dto.LinkRequestDto;
import systemdesigns.study.Bitly.dto.LinkResponseDto;
import systemdesigns.study.Bitly.service.LinkService;

@RestController
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @PostMapping(value = "/api/links")
    public ResponseEntity<LinkResponseDto> shortUrl(@RequestBody LinkRequestDto requestDto){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(linkService.createLink(requestDto));
    }
}
