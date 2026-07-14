package org.app.f1.controller;

import lombok.RequiredArgsConstructor;
import org.app.f1.service.StandingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/standings")
public class StandingsController {

    private final StandingsService standingsService;

    @GetMapping("/drivers/{year}")
    public List<?> getDriverStandings(
            @PathVariable int year
    ) {
        return standingsService.getDriverStandings(year);
    }

    @GetMapping("/teams/{year}")
    public List<?> getConstructorsStandings(
            @PathVariable int year
    ) {
        return standingsService.getTeamStandings(year);
    }
}
