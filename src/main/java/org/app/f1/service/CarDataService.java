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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

    @Cacheable(value = "car_data", key = "#sessionKey + '-' + #lapNumber + '-' + #driverNumber", unless = "#result.isEmpty()")
    public List<CarData> loadCarDataByLap(int sessionKey, int lapNumber, int driverNumber) {

        Lap lap = lapService.getLap(lapNumber, sessionKey, driverNumber);
        Instant lapEnd = lap.getLapEnd();
        var data = carDataRepo.loadCarData(sessionKey, lap.getLapStart(), lapEnd, driverNumber);
        if (!data.isEmpty()) {
            return interpolateCarData(data);
        }

        Session session = sessionService.fetchSession(sessionKey);
        List<CarDataRequest> requests = openF1Client.getCarData(driverNumber, lap.getLapStart(), lapEnd, sessionKey);
        List<CarData> carData = requests.stream().map(rq -> rq.buildEntity(session, lapNumber)).toList();

        dataImportService.saveAllCarData(carData);
        return interpolateCarData(carData);
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

    public List<CarData> interpolateCarData(List<CarData> data) {

        if (data == null || data.size() < 2) {
            return data;
        }

        System.out.println("Raw data size " +  data.size());

        CarData first = data.get(0);
        CarData second = data.get(1);
        CarData last = data.get(data.size() - 1);

        long curr = first.getDate().toEpochMilli(); // current time
        long lst = last.getDate().toEpochMilli(); // last time

        Integer driverNumber = first.getDriverNumber();
        Session session = first.getSession();
        Integer lapNumber = first.getLapNumber();

        List<CarData> processed = new ArrayList<>();
        processed.add(first);

        int idx = 2;
        while (curr < lst) {
            curr += 50;

            if (curr >= lst) {
                break;
            }

            long t1 = second.getDate().toEpochMilli();

            while (curr > t1 && idx < data.size()) {
                first = second;
                second = data.get(idx++);
                t1 = second.getDate().toEpochMilli();
            }

            long t0 = first.getDate().toEpochMilli();

            long dx1 = t1 - curr;
            long dx0 = curr - t0;
            long dx10 = t1 - t0;

            if (dx10 == 0) continue;

            int speed = (int) ((first.getSpeed() * dx1 + second.getSpeed() * dx0) / dx10);
            int throttle = (int) ((first.getThrottle() * dx1 + second.getThrottle() * dx0) / dx10);

            boolean brake = dx1 > dx0 ? first.getBrake() : second.getBrake();
            int gear = dx1 > dx0 ? first.getGear() : second.getGear();
            Integer drs = dx1 > dx0 ? first.getDrs() : second.getDrs();
            if (drs == null) { drs = 0; }

            CarData cd = CarData.builder()
                    .driverNumber(driverNumber)
                    .lapNumber(lapNumber)
                    .session(session)
                    .date(Instant.ofEpochMilli(curr))
                    .speed(speed)
                    .throttle(throttle)
                    .brake(brake)
                    .gear(gear)
                    .drs(drs)
                    .build();

            processed.add(cd);
        }
        processed.add(last);

        return processed;
    }
}
