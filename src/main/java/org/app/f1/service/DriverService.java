package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import org.app.f1.dto.request.DriverRequest;
import org.app.f1.dto.response.DriverResponse;
import org.app.f1.entities.Driver;
import org.app.f1.entities.DriverEntry;
import org.app.f1.entities.Session;
import org.app.f1.entities.Team;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.DriverRepo;
import org.app.f1.repositories.DriverEntryRepo;
import org.app.f1.repositories.SessionRepo;
import org.app.f1.repositories.TeamRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverEntryRepo driverEntryRepo;
    private final DriverRepo driverRepo;
    private final TeamRepo teamRepo;
    private final SessionRepo sessionRepo;
    private final SessionService sessionService;
    private final OpenF1Client openF1Client;

    @Transactional
    @Cacheable(value = "drivers", key = "#sessionKey")
    public List<DriverResponse> getDrivers(int sessionKey) {
        Session session = sessionService.fetchSession(sessionKey);
        int year = session.getMeeting().getYear();

        Set<Driver> drivers = session.getDrivers();
        if (drivers.isEmpty() || driverEntryRepo.countByDriverInAndSeason(drivers, year) < drivers.size()) {
            drivers = fetchAndSaveDrivers(session, year);
        }

        return buildDriverResponses(drivers, year);
    }

    private Set<Driver> fetchAndSaveDrivers(Session session, int year) {
        List<DriverRequest> driverRequests = openF1Client.getDriverRequests(session);

        if (driverRequests.isEmpty()) {
            throw new ResourceNotFoundException("No drivers found for session: " + session.getSessionKey());
        }

        List<Driver> drivers = driverRequests.stream()
                .map(request -> processDriverRequest(request, year))
                .toList();

        session.getDrivers().clear();
        session.getDrivers().addAll(drivers);
        sessionRepo.save(session);

        return session.getDrivers();
    }

    public DriverEntry getDriverEntry(Driver driver, int year) {
        return driverEntryRepo.findByDriverAndSeason(driver, year)
                .orElseThrow(() -> new ResourceNotFoundException("Driver entry could not found!"));
    }

    private Driver processDriverRequest(DriverRequest request, int year) {
        Driver driver = findOrCreateDriver(request);
        Team team = findOrCreateTeam(request);
        ensureDriverEntryExists(driver, team, request.driverNumber(), request.nameAcronym(), year);
        return driver;
    }

    private Driver findOrCreateDriver(DriverRequest request) {
        String normalizedName = Driver.normalize(request.fullName());
        return driverRepo.findByNormalizedName(normalizedName)
                .orElseGet(() -> driverRepo.save(request.buildEntity()));
    }

    private Team findOrCreateTeam(DriverRequest request) {
        return teamRepo.findByName(request.teamName())
                .orElseGet(() -> teamRepo.save(request.buildTeamEntity()));
    }

    private void ensureDriverEntryExists(Driver driver, Team team, Integer driverNumber, String acronym, int year) {
        if (driverEntryRepo.findByDriverAndSeason(driver, year).isEmpty()) {
            DriverEntry driverEntry = DriverEntry.builder()
                    .driver(driver)
                    .team(team)
                    .driverNumber(driverNumber)
                    .acronym(acronym)
                    .season(year)
                    .build();
            driverEntryRepo.save(driverEntry);
        }
    }

    private List<DriverResponse> buildDriverResponses(Set<Driver> drivers, int year) {
        Map<Long, DriverEntry> entryMap = buildDriverEntryMap(drivers, year);

        return drivers.stream()
                .map(driver -> DriverResponse.fromEntity(driver, entryMap.get(driver.getId())))
                .toList();
    }

    private Map<Long, DriverEntry> buildDriverEntryMap(Set<Driver> drivers, int year) {
        List<DriverEntry> entries = driverEntryRepo.findByDriverInAndSeason(drivers, year);
        return entries.stream()
                .collect(Collectors.toMap(e -> e.getDriver().getId(), Function.identity()));
    }
}
