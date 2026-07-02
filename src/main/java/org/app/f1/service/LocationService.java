package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import org.app.f1.dto.request.LocationRequest;
import org.app.f1.dto.response.LocationResponse;
import org.app.f1.entities.Location;
import org.app.f1.entities.Session;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.LocationRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final OpenF1Client openF1Client;
    private final LocationRepo locationRepo;
    private final LapService lapService;
    private final SessionService sessionService;
    private final DataImportService dataImportService;

    public List<LocationResponse> loadLocations(int sessionKey, int lapNumber, int driverNumber) {
        var lap = lapService.getLap(lapNumber, sessionKey, driverNumber);
        var locations = locationRepo.loadLocations(sessionKey, driverNumber, lap.getLapStart(), lap.getLapEnd());
        if (!locations.isEmpty()) {
            return locations.stream().map(LocationResponse::fromEntity).toList();
        }

        Session session = sessionService.fetchSession(sessionKey);
        List<LocationRequest> requests = openF1Client.getLocations(sessionKey, driverNumber, lap.getLapStart(), lap.getLapEnd());

        if (requests.isEmpty()) {
            throw new ResourceNotFoundException("No location data found for driver " + driverNumber + " on lap " + lapNumber);
        }

        List<Location> fetched = requests.stream().map(r -> r.buildEntity(session)).toList();
        dataImportService.saveAllLocations(fetched);
        return fetched.stream().map(LocationResponse::fromEntity).toList();
    }
}
