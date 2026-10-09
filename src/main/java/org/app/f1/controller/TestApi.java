package org.app.f1.controller;

import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.CarDataResponse;
import org.app.f1.entities.CarData;
import org.app.f1.service.CarDataService;
import org.app.f1.service.CustomRedisService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/testApi")
public class TestApi {

    private final CustomRedisService rds;
    private final CarDataService cds;

    @PostMapping("/1")
    public String testApi() {
        List<CarDataResponse> cd = cds.getCarDataResponse(9159, 25, 55);
        if (cd.isEmpty()) {
            return "fail";
        }
        return "success";
    }

    @PostMapping("/2")
    public List<CarDataResponse> testApi2() {
        return cds.getCarDataResponse(9159, 25, 55);
    }
}
