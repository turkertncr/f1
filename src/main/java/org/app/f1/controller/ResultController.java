package org.app.f1.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.f1.dto.response.ResultResponse;
import org.app.f1.service.ResultService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/results")
public class ResultController {

    private final ResultService resultService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ResultResponse> findAllBySession(@Positive @RequestParam int sessionKey) {
        return resultService.findAllBySession(sessionKey);
    }
}
