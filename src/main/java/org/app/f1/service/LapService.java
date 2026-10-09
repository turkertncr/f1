package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.f1.dto.request.LapRequest;
import org.app.f1.dto.response.DriverResponse;
import org.app.f1.entities.*;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.DriverEntryRepo;
import org.app.f1.repositories.LapRepo;
import org.app.f1.repositories.SectorRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class LapService {

    private final OpenF1Client openF1Client;
    private final SessionService sessionService;
    private final DataImportService dataImportService;
    private final DriverEntryRepo driverEntryRepo;
    private final LapRepo lapRepo;
    private final SectorRepo sectorRepo;
    private final DriverService driverService;

    @Value("${outlier_threshold}")
    private double OUTLIER_THRESHOLD;

    public List<Lap> getLaps(int sessionKey, int driverNumber) {
        if (sectorRepo.existsByLapSessionSessionKeyAndLapDriverNumber(sessionKey, driverNumber)) {
            return lapRepo.findAllBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        }

        List<Lap> laps = lapRepo.getAllBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        List<LapRequest> requests = openF1Client.getLaps(sessionKey, driverNumber);

        if (laps.isEmpty()) {
            Session session = sessionService.fetchSession(sessionKey);
            double median = computeMedian(requests);
            dataImportService.saveAllLaps(filterOutlier(requests, session, median));
            laps = lapRepo.getAllBySessionKeyAndDriverNumber(sessionKey, driverNumber);
        }

        saveSectors(laps, requests);

        return lapRepo.findAllBySessionKeyAndDriverNumber(sessionKey, driverNumber);
    }

    private void saveSectors(List<Lap> savedLaps, List<LapRequest> requests) {
        Map<String, Lap> byDriverAndLapNumber = savedLaps.stream()
                .collect(Collectors.toMap(l -> l.getDriverNumber() + "-" + l.getLapNumber(), l -> l));

        List<Sector> sectors = requests.stream()
                .flatMap(request -> {
                    Lap lap = byDriverAndLapNumber.get(request.driverNumber() + "-" + request.lapNumber());
                    if (lap == null) {
                        return Stream.<Sector>empty();
                    }
                    return Stream.of(
                            buildSector(lap, 1, request.sectorDuration1(), request.sectorSegments1()),
                            buildSector(lap, 2, request.sectorDuration2(), request.sectorSegments2()),
                            buildSector(lap, 3, request.sectorDuration3(), request.sectorSegments3()));
                })
                .toList();

        if (!sectors.isEmpty()) {
            dataImportService.saveAllSectors(sectors);
        }
    }

    private List<Lap> filterOutlier(List<LapRequest> laps, Session session, double median) {
        return laps.stream()
                .map(request -> {
                    boolean isOutlier = request.duration() != null
                            && request.duration() > median * OUTLIER_THRESHOLD;
                    return request.buildEntity(session, isOutlier);
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
            saveSectors(allLaps, requestedLaps);
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

    private Sector buildSector(Lap lap, int order, Double duration, List<Integer> segments) {
        return Sector.builder()
                .lap(lap)
                .sector(order)
                .time(duration)
                .segments(segments)
                .build();
    }
}
