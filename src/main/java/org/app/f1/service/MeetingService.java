package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.MeetingResponse;
import org.app.f1.entities.Meeting;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.repositories.MeetingRepo;
import org.app.f1.service.openf1.OpenF1Client;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepo meetingRepo;
    private final OpenF1Client openF1Client;
    private final DataImportService dataImportService;

    public List<Meeting> loadAllByYear(int year) {
        List<Meeting> meetings = meetingRepo.findAllByYear(year);
        if (meetings.isEmpty()) {
            meetings = openF1Client.getMeetings(year);
            if (!meetings.isEmpty()) {
                dataImportService.saveAllMeetings(meetings);
            } else {
                throw new ResourceNotFoundException("No meetings found for year " + year);
            }
        }
        return meetings;
    }

    @Cacheable(value = "meeting_responses", key = "#year", unless = "#result.isEmpty()")
    public List<MeetingResponse> getMeetingResponse(int year) {
        return loadAllByYear(year).stream().map(MeetingResponse::fromEntity).toList();
    }
}
