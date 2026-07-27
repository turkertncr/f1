package org.app.f1.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.app.f1.dto.response.MeetingResponse;
import org.app.f1.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MeetingResponse> loadAllMeetings(@Min(1950) @Max(2100) @RequestParam int year) {
        return meetingService.getMeetingResponse(year);
    }
}
