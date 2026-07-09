package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.f1.dto.request.LapRequest;
import org.app.f1.entities.*;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.DriverEntryRepo;
import org.app.f1.repositories.LapRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LapService {

    private final OpenF1Client openF1Client;
    private final SessionService sessionService;
    private final DataImportService dataImportService;
    private final DriverEntryRepo driverEntryRepo;
    private final LapRepo lapRepo;

    @Value("${outlier_threshold}")
    private double OUTLIER_THRESHOLD;

    @Cacheable(value = "laps", key = "#sessionKey + '-' + #driverNumber", unless = "#result.isEmpty()")
    public List<Lap> getLaps(int sessionKey, int driverNumber) {
        var laps = lapRepo.findAllBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        if (!laps.isEmpty()) {
            return laps;
        }

        Session session = sessionService.fetchSession(sessionKey);
        List<LapRequest> requests = openF1Client.getLaps(sessionKey, driverNumber);

        double median = computeMedian(requests);
        List<Lap> newLaps = filterOutlier(requests, session, median);

        dataImportService.saveAllLaps(newLaps);

        List<Lap> savedLaps = lapRepo.findAllBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        Map<Integer, Lap> byLapNumber = savedLaps.stream()
                .collect(Collectors.toMap(Lap::getLapNumber, l -> l));

        List<Sector> sectors = newLaps.stream()
                .filter(l -> byLapNumber.containsKey(l.getLapNumber()))
                .flatMap(l -> l.getSectors().stream()
                        .peek(s -> s.setLap(byLapNumber.get(l.getLapNumber()))))
                .toList();

        if (!sectors.isEmpty()) {
            dataImportService.saveAllSectors(sectors);
        }

        return lapRepo.findAllBySessionKeyAndDriverNumber(sessionKey, driverNumber);
    }

    private List<Lap> filterOutlier(List<LapRequest> laps, Session session, double median) {
        return laps.stream()
                .map(request -> {
                    boolean isOutlier = request.duration() != null
                            && request.duration() > median * OUTLIER_THRESHOLD;
                    return toLap(request, session, isOutlier);
                })
                .toList();
    }

    public Lap getLap(int lapNumber, int sessionKey, int driverNumber) {
        return lapRepo.findBySessionKeyAndDriverNumberAndLapNumber(sessionKey, driverNumber, lapNumber)
                .orElseGet(() -> getLaps(sessionKey, driverNumber).stream()
                        .filter(l -> l.getLapNumber() == lapNumber)
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Lap " + lapNumber + " not found for driver " + driverNumber)));
    }

    @Cacheable(value = "allLaps", key = "#sessionKey", unless = "#result.isEmpty()")
    public List<Lap> getAllLaps(int sessionKey) {
        Session session = sessionService.fetchSession(sessionKey);
        var allLaps = lapRepo.getAllBySessionKey(sessionKey);

        Set<Integer> numbers = allLaps.stream()
                .map(Lap::getDriverNumber)
                .collect(Collectors.toSet());

        int year = session.getMeeting().getYear();
        Set<Integer> sessionDriverNumbers = driverEntryRepo
                .findByDriverInAndSeason(session.getDrivers(), year)
                .stream()
                .map(DriverEntry::getDriverNumber)
                .collect(Collectors.toSet());

        boolean shouldFetch = !numbers.containsAll(sessionDriverNumbers);

        if (shouldFetch) {
            List<LapRequest> requestedLaps = openF1Client.getAllLaps(sessionKey);
            double median = computeMedian(requestedLaps);
            dataImportService.saveAllLaps(filterOutlier(requestedLaps, session, median));
            allLaps = lapRepo.getAllBySessionKey(sessionKey);
        }
        return allLaps;
    }

    private double computeMedian(List<LapRequest> requests) {
        List<Double> durations = requests.stream()
                .filter(r -> r.duration() != null && (r.isPitLap() == null || !r.isPitLap()))
                .map(LapRequest::duration)
                .sorted()
                .toList();
        if (durations.isEmpty()) return Double.MAX_VALUE;
        return durations.get(durations.size() / 2);
    }

    private Lap toLap(LapRequest request, Session session, boolean isOutlier) {
        Lap lap = request.buildEntity(session, isOutlier);
        lap.getSectors().add(buildSector(lap, 1, request.sectorDuration1(), request.sectorSegments1()));
        lap.getSectors().add(buildSector(lap, 2, request.sectorDuration2(), request.sectorSegments2()));
        lap.getSectors().add(buildSector(lap, 3, request.sectorDuration3(), request.sectorSegments3()));
        return lap;
    }

    private Sector buildSector(Lap lap, int order, Double duration, List<Integer> segments) {
        return Sector.builder()
                .lap(lap)
                .sector(order)
                .time(duration)
                .segments(segments)
                .build();
    }
}
