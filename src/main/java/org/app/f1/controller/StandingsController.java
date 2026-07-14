package org.app.f1.controller;

import lombok.RequiredArgsConstructor;
import org.app.f1.service.StandingsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/standings")
public class StandingsController {

    private final StandingsService standingsService;

    @GetMapping("/drivers")
    public List<?> getDriverStandings(
            @RequestParam int year
    ) {
        return standingsService.getDriverStandings(year);
    }

    @GetMapping("/teams")
    public List<?> getConstructorsStandings(
            @RequestParam int year
    ) {
        return standingsService.getTeamStandings(year);
    }
}
