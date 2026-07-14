package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.DriverStandingsResponse;
import org.app.f1.dto.response.TeamStandingsResponse;
import org.app.f1.entities.Driver;
import org.app.f1.entities.DriverEntry;
import org.app.f1.entities.DriverStandings;
import org.app.f1.entities.Team;
import org.app.f1.entities.TeamStandings;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.DriverEntryRepo;
import org.app.f1.repositories.DriverStandingsRepo;
import org.app.f1.repositories.TeamRepo;
import org.app.f1.repositories.TeamStandingsRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StandingsService {

    private final SessionService sessionService;
    private final TeamStandingsRepo teamStandingsRepo;

    private final OpenF1Client openF1Client;
    private final DriverStandingsRepo driverStandingsRepo;

    private final TeamRepo teamRepo;
    private final DriverEntryRepo driverEntryRepo;
    private final DriverService driverService;

    public List<TeamStandingsResponse> getTeamStandings(int year) {

        int sessionKey = sessionService.getLastSessionKeyByYear(year);
        List<TeamStandings> standings = teamStandingsRepo.findBySessionKey(sessionKey);

        if (standings.isEmpty()) {
            standings = openF1Client.getTeamsStandings(sessionKey);
            if (standings.isEmpty()) {
                throw new ResourceNotFoundException("No team standings found for session key " + sessionKey);
            }
            teamStandingsRepo.saveAll(standings);
        }

        Map<String, Team> teamMap = teamRepo.getTeamMap();

        boolean missingTeams = standings.stream()
                .anyMatch(std -> !teamMap.containsKey(std.getTeamName()));
        Map<String, Team> resolvedTeams = missingTeams
                ? warmDriversAndReloadTeams(sessionKey)
                : teamMap;

        return standings.stream()
                .map(std -> TeamStandingsResponse.fromEntity(std, resolvedTeams.get(std.getTeamName())))
                .toList();
    }

    public List<DriverStandingsResponse> getDriverStandings(int year) {

        int sessionKey = sessionService.getLastSessionKeyByYear(year);
        List<DriverStandings> standings = driverStandingsRepo.findBySessionKey(sessionKey);

        if (standings.isEmpty()) {
            standings = openF1Client.getDriversStandings(sessionKey);
            if (standings.isEmpty()) {
                throw new ResourceNotFoundException("No driver standings found for session key " + sessionKey);
            }
            driverStandingsRepo.saveAll(standings);
        }

        Map<Integer, Driver> driverMap = getDriverMap(year);

        boolean missingDrivers = standings.stream()
                .anyMatch(std -> !driverMap.containsKey(std.getDriverNumber()));
        Map<Integer, Driver> resolvedDrivers = missingDrivers
                ? warmDriversAndReloadDrivers(sessionKey, year)
                : driverMap;
        return standings.stream()
                .map(std -> DriverStandingsResponse.fromEntity(std, resolvedDrivers.get(std.getDriverNumber())))
                .toList();
    }

    private Map<String, Team> warmDriversAndReloadTeams(int sessionKey) {
        driverService.getDrivers(sessionKey);
        return teamRepo.getTeamMap();
    }

    private Map<Integer, Driver> warmDriversAndReloadDrivers(int sessionKey, int year) {
        driverService.getDrivers(sessionKey);
        return getDriverMap(year);
    }

    private Map<Integer, Driver> getDriverMap(int year) {
        return driverEntryRepo.findBySeason(year).stream()
                .collect(Collectors.toMap(DriverEntry::getDriverNumber, DriverEntry::getDriver, (first, second) -> first));
    }
}
