package org.app.f1.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.CarDataResponse;
import org.app.f1.service.CarDataService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/car_data")
public class CarDataController {

    private final CarDataService carDataService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CarDataResponse> getCarData(
            @Positive @RequestParam int driverNumber,
            @Positive @RequestParam int sessionKey,
            @Positive @RequestParam int lapNumber
    ) {
        return carDataService.getCarDataResponse(sessionKey, lapNumber, driverNumber);
    }
}
