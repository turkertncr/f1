package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.f1.dto.response.DriverStandingsResponse;
import org.app.f1.dto.response.TeamStandingsResponse;
import org.app.f1.entities.Driver;
import org.app.f1.entities.DriverEntry;
import org.app.f1.entities.DriverStandings;
import org.app.f1.entities.Standings;
import org.app.f1.entities.Team;
import org.app.f1.entities.TeamStandings;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.DriverEntryRepo;
import org.app.f1.repositories.DriverStandingsRepo;
import org.app.f1.repositories.StandingsRepo;
import org.app.f1.repositories.TeamRepo;
import org.app.f1.repositories.TeamStandingsRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
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

    @Cacheable(value = "teams_standings", key = "#year", sync = true)
    public List<TeamStandingsResponse> getTeamStandings(int year) {

        int sessionKey = sessionService.getLastSessionKeyByYear(year);
        List<TeamStandings> standings = loadStandings(
                teamStandingsRepo, sessionKey, "team", () -> openF1Client.getTeamsStandings(sessionKey));

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

    @Cacheable(value = "drivers_standings", key = "#year", sync = true)
    public List<DriverStandingsResponse> getDriverStandings(int year) {

        int sessionKey = sessionService.getLastSessionKeyByYear(year);
        List<DriverStandings> standings = loadStandings(
                driverStandingsRepo, sessionKey, "driver", () -> openF1Client.getDriversStandings(sessionKey));

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

    private <T extends Standings> List<T> loadStandings(StandingsRepo<T> repo, int sessionKey, String label, Supplier<List<T>> fetch) {
        List<T> standings = repo.findBySessionKey(sessionKey);
        if (!standings.isEmpty()) {
            return standings;
        }

        List<T> fetched = fetch.get();
        if (fetched.isEmpty()) {
            throw new ResourceNotFoundException("No " + label + " standings found for session key " + sessionKey);
        }

        List<T> distinct = new ArrayList<>(new LinkedHashSet<>(fetched));
        try {
            return repo.saveAll(distinct);
        } catch (DataIntegrityViolationException e) {
            List<T> persisted = repo.findBySessionKey(sessionKey);
            if (persisted.isEmpty()) {
                throw e;
            }
            log.debug("Concurrent write of {} standings for session {} won the race, using persisted rows",
                    label, sessionKey);
            return persisted;
        }
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
