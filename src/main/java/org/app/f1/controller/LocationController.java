package org.app.f1.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.LocationResponse;
import org.app.f1.service.LocationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public List<LocationResponse> getLocations(
            @Positive @RequestParam int sessionKey,
            @Positive @RequestParam int driverNumber,
            @Positive @RequestParam int lapNumber
    ) {
        return locationService.loadLocations(sessionKey, lapNumber, driverNumber);
    }
}
