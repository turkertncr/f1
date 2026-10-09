package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.f1.dto.response.DriverResponse;
import org.app.f1.dto.response.ResultResponse;
import org.app.f1.entities.Result;
import org.app.f1.entities.Session;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.ResultRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepo resultRepo;
    private final SessionService sessionService;
    private final OpenF1Client openF1Client;
    private final DriverService driverService;
    private final DataImportService dataImportService;

    @Cacheable(value = "results", key = "#sessionKey", unless = "#result.isEmpty()")
    public List<ResultResponse> findAllBySession(int sessionKey) {

        Set<Result> results = resultRepo.findAllBySession_SessionKey(sessionKey);
        List<DriverResponse> drivers = driverService.getDrivers(sessionKey);

        Map<Integer, DriverResponse> map = new HashMap<>();
        for (DriverResponse driver : drivers) {
            map.put(driver.driverNumber(), driver);
        }

        if (!results.isEmpty()) {
            return results.stream()
                    .map(res -> ResultResponse.fromEntity(res, resolveDriver(res.getDriverNumber(), map)))
                    .sorted(BY_POSITION)
                    .toList();
        }

        Session session = sessionService.fetchSession(sessionKey);
        List<Result> fetched = openF1Client.getResults(session);

        if (fetched.isEmpty()) {
            throw new ResourceNotFoundException("Result with key %d not found".formatted(sessionKey));
        }

        Set<Result> unique = new HashSet<>(fetched);
        unique.forEach(result -> result.setSession(session));
        try {
            dataImportService.saveAllResults(unique.stream().toList());
        } catch (DataIntegrityViolationException e) {
            return resultRepo.findAllBySession_SessionKey(sessionKey).stream()
                    .map(res -> ResultResponse.fromEntity(res, resolveDriver(res.getDriverNumber(), map)))
                    .sorted(BY_POSITION)
                    .toList();
        }

        return unique.stream()
                .map(res -> ResultResponse.fromEntity(res, resolveDriver(res.getDriverNumber(), map)))
                .sorted(BY_POSITION)
                .toList();
    }

    private static final Comparator<ResultResponse> BY_POSITION =
            Comparator.comparing(ResultResponse::position, Comparator.nullsLast(Comparator.naturalOrder()));

    private DriverResponse resolveDriver(int driverNumber, Map<Integer, DriverResponse> map) {
        DriverResponse found = map.get(driverNumber);
        if (found != null) return found;
        return DriverResponse.builder()
                .driverNumber(driverNumber)
                .broadcastName("Driver #" + driverNumber)
                .fullName("Driver #" + driverNumber)
                .nameAcronym("D" + driverNumber)
                .teamName("Unknown")
                .teamColour("333333")
                .headshotUrl("")
                .countryCode("")
                .build();
    }
}
