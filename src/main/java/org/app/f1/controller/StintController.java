package org.app.f1.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.StintResponse;
import org.app.f1.service.StintService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stints")
public class StintController {

    private final StintService stintService;

    @GetMapping
    public List<StintResponse> findStints(@Positive @RequestParam int driverNumber, @Positive @RequestParam int sessionKey) {
        return stintService.findStintsByDriverNumber(driverNumber, sessionKey);
    }
}
