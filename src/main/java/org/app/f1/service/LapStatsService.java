package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.f1.dto.response.DriverResponse;
import org.app.f1.dto.response.LapResponse;
import org.app.f1.dto.response.PaceResponse;
import org.app.f1.entities.CarData;
import org.app.f1.entities.Driver;
import org.app.f1.entities.Lap;
import org.app.f1.entities.Session;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LapStatsService {

    private final LapService lapService;
    private final CarDataService carDataService;
    private final DriverService driverService;
    private final SessionService sessionService;

    public List<LapResponse> getLapResponses(int sessionKey, int driverNumber) {
        var carDataMap = carDataService.getCarDataFromDb(sessionKey, driverNumber);
        return lapService.getLaps(sessionKey, driverNumber).stream()
                .map(lap -> {
                    var lapList = carDataMap.getOrDefault(lap.getLapNumber(), List.of());
                    IntSummaryStatistics stats = lapList.stream()
                            .filter(Objects::nonNull)
                            .map(CarData::getSpeed)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .summaryStatistics();
                    int avgSpeed = stats.getCount() > 0 ? (int) stats.getAverage() : 0;
                    int topSpeed = stats.getCount() > 0 ? stats.getMax() : 0;
                    return LapResponse.fromEntity(lap, avgSpeed, topSpeed);
                })
                .toList();
    }

    private Map<Integer, Double> getPaces(int sessionKey) {
        var list = lapService.getAllLaps(sessionKey);
        Map<Integer, List<Lap>> map = new HashMap<>();

        for (Lap lap : list) {
            map.computeIfAbsent(lap.getDriverNumber(), k -> new ArrayList<>()).add(lap);
        }

        Map<Integer, Double> paces = new HashMap<>();

        map.forEach((k, v) -> {
            DoubleSummaryStatistics stats = v.stream()
                    .filter(Objects::nonNull)
                    .filter(lap -> !lap.isOutlier() && !lap.isPitLap())
                    .map(Lap::getDuration)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .summaryStatistics();

            if (stats.getCount() == 0) {
                return;
            }
            paces.put(k, stats.getAverage());
        });
        return paces;
    }

    public List<PaceResponse> buildPaceResponse(int sessionKey) {
        List<PaceResponse> paces = new ArrayList<>();

        Session session = sessionService.fetchSession(sessionKey);
        var sessionDrivers = session.getDrivers();

        var map = getPaces(sessionKey);

        for (Driver driver : sessionDrivers) {
            var entry = driverService.getDriverEntry(driver, session.getMeeting().getYear());
            Double pace = map.get(entry.getDriverNumber());
            if (pace == null) {
                continue;
            }

            paces.add(
                    new  PaceResponse(DriverResponse.fromEntity(driver, entry), pace)
            );
        }
        return paces;
    }
}
