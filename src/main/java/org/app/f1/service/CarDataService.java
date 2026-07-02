package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.f1.dto.request.CarDataRequest;
import org.app.f1.dto.response.CarDataResponse;
import org.app.f1.entities.CarData;
import org.app.f1.entities.Lap;
import org.app.f1.entities.Session;
import org.app.f1.repositories.CarDataRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarDataService {

    private final CarDataRepo carDataRepo;
    private final SessionService sessionService;
    private final LapService lapService;
    private final OpenF1Client openF1Client;
    private final DataImportService dataImportService;

    @Cacheable(value = "car_data", key = "#sessionKey + '-' + #lapNumber + '-' + #driverNumber")
    public List<CarData> loadCarDataByLap(int sessionKey, int lapNumber, int driverNumber) {

        Lap lap = lapService.getLap(lapNumber, sessionKey, driverNumber);
        Instant lapEnd = lap.getLapEnd();
        var data = carDataRepo.loadCarData(sessionKey, lap.getLapStart(), lapEnd, driverNumber);
        if (!data.isEmpty()) {
            return data;
        }

        Session session = sessionService.fetchSession(sessionKey);
        List<CarDataRequest> requests = openF1Client.getCarData(driverNumber, lap.getLapStart(), lapEnd, sessionKey);
        List<CarData> carData = requests.stream().map(rq -> rq.buildEntity(session, lapNumber)).toList();

        dataImportService.saveAllCarData(carData);
        return carData;
    }

    public List<CarDataResponse> getCarDataResponse(int sessionKey, int lapNumber, int driverNumber) {
        return loadCarDataByLap(sessionKey, lapNumber, driverNumber)
                .stream()
                .map(CarDataResponse::fromEntity).toList();
    }

    public Map<Integer, List<CarData>> getCarDataFromDb(int sessionKey, int driverNumber) {
        return carDataRepo.getAllByDriverNumberAndSession(driverNumber, sessionKey)
                .stream()
                .collect(Collectors.groupingBy(CarData::getLapNumber));
    }
}
