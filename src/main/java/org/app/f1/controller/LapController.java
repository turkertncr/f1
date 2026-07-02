package org.app.f1.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.LapResponse;
import org.app.f1.dto.response.PaceResponse;
import org.app.f1.service.LapStatsService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RequestMapping("/api/v1/laps")
@RestController
@RequiredArgsConstructor
public class LapController {

    private final LapStatsService lapStatsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<LapResponse> getLaps(@Positive @RequestParam int sessionKey, @Positive @RequestParam int driverNumber) {
        return lapStatsService.getLapResponses(sessionKey, driverNumber);
    }

    @GetMapping("/pace")
    @ResponseStatus(HttpStatus.OK)
    public List<PaceResponse> getPace(@Positive @RequestParam int sessionKey) {
        return lapStatsService.buildPaceResponse(sessionKey);
    }
}
