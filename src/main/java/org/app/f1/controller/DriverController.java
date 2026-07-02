package org.app.f1.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.DriverResponse;
import org.app.f1.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<DriverResponse> getDrivers(@Positive @RequestParam int sessionKey) {
        return driverService.getDrivers(sessionKey);
    }

}
