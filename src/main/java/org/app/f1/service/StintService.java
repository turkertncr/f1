package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.StintResponse;
import org.app.f1.entities.Session;
import org.app.f1.entities.Stint;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.StintRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StintService {

    private final StintRepo stintRepo;
    private final SessionService sessionService;
    private final OpenF1Client openF1Client;
    private final DataImportService dataImportService;

    @Cacheable(value = "stints", key = "#sessionKey + '-' + #driverNumber", unless = "#result.isEmpty()")
    public List<StintResponse> findStintsByDriverNumber(int driverNumber, int sessionKey) {

        Optional<List<Stint>> stint = stintRepo.findStintsByDriverNumberAndSessionSessionKeyOrderByStintNumberAsc(driverNumber, sessionKey);
        List<Stint> stints;

        if (stint.isEmpty() || stint.get().isEmpty()) {
            Session session = sessionService.fetchSession(sessionKey);
            stints = openF1Client.getStints(driverNumber, session);
            if (!stints.isEmpty()) {
                stints.forEach(st -> {
                    st.setMeeting(session.getMeeting());
                    st.setSession(session);
                });
                dataImportService.saveAllStints(stints);
            } else {
                throw new ResourceNotFoundException("Stints could not be fetched for driver: " + driverNumber);
            }
        } else {
            stints = stint.get();
        }
        return stints.stream().map(StintResponse::fromEntity).toList();
    }
}
