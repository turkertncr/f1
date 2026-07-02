package org.app.f1.service.openf1;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.f1.dto.EntityMapper;
import org.app.f1.dto.request.*;
import org.app.f1.entities.*;
import org.app.f1.exception.ResourceNotFoundException;
import org.app.f1.exception.ServiceUnavailableException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class OpenF1Client {

    private static final String RETRY_NAME = "openF1Retry";
    private static final String RATE_LIMITER_NAME = "openF1RateLimiter";
    private static final String CIRCUIT_BREAKER_NAME = "openF1CircuitBreaker";

    private final WebClient webClient;

    @Retry(name = RETRY_NAME, fallbackMethod = "getMeetingsFallback")
    @RateLimiter(name = RATE_LIMITER_NAME, fallbackMethod = "getMeetingsFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getMeetingsFallback")
    public List<Meeting> getMeetings(int year) {
        log.debug("Fetching meetings for year: {}", year);
        return fetchAndMap(
                OpenF1Endpoints.meetings,
                Map.of("year", year),
                new ParameterizedTypeReference<>() {
                },
                MeetingRequest::buildEntity
        );
    }

    private List<Meeting> getMeetingsFallback(int year, Throwable t) {
        log.error("Fallback triggered for getMeetings(year={}): {}", year, t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getSessionsFallback")
    @RateLimiter(name = RATE_LIMITER_NAME, fallbackMethod = "getSessionsFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getSessionsFallback")
    public List<Session> getSessions(Meeting meeting) {
        log.debug("Fetching sessions for meeting: {} (key: {})", meeting.getName(), meeting.getMeetingKey());
        return fetchAndMap(
                OpenF1Endpoints.sessions,
                Map.of("meeting_key", meeting.getMeetingKey()),
                new ParameterizedTypeReference<List<SessionRequest>>() {
                },
                dto -> dto.buildEntity(meeting)
        );
    }

    private List<Session> getSessionsFallback(Meeting meeting, Throwable t) {
        log.error("Fallback triggered for getSessions(meetingKey={}): {}", meeting.getMeetingKey(), t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getResultsFallback")
    @RateLimiter(name = RATE_LIMITER_NAME, fallbackMethod = "getResultsFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getResultsFallback")
    public List<Result> getResults(Session session) {
        log.debug("Fetching results for session: {} (key: {})", session.getSessionName(), session.getSessionKey());
        return fetchAndMap(
                OpenF1Endpoints.result,
                Map.of("session_key", session.getSessionKey()),
                new ParameterizedTypeReference<>() {
                },
                ResultRequest::buildEntity
        );
    }

    private List<Result> getResultsFallback(Session session, Throwable t) {
        log.error("Fallback triggered for getResults(sessionKey={}): {}", session.getSessionKey(), t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getDriversFallback")
    @RateLimiter(name = RATE_LIMITER_NAME, fallbackMethod = "getDriversFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getDriversFallback")
    public List<Driver> getDrivers(Session session) {
        log.debug("Fetching drivers for session: {}", session.getSessionKey());
        return fetchAndMap(
                OpenF1Endpoints.drivers,
                Map.of("session_key", session.getSessionKey()),
                new ParameterizedTypeReference<>() {
                },
                DriverRequest::buildEntity
        );
    }

    private List<Driver> getDriversFallback(Session session, Throwable t) {
        log.error("Fallback triggered for getDrivers(sessionKey={}): {}", session.getSessionKey(), t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getStintsFallback")
    @RateLimiter(name = RATE_LIMITER_NAME, fallbackMethod = "getStintsFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getStintsFallback")
    public List<Stint> getStints(int driverNumber, Session session) {
        log.debug("Fetching stint requests for driver: {}, session: {}", driverNumber, session.getSessionKey());
        return fetchAndMap(
                OpenF1Endpoints.stints,
                Map.of("driver_number", driverNumber, "session_key", session.getSessionKey()),
                new ParameterizedTypeReference<List<StintRequest>>() {
                },
                dto -> dto.buildEntity(session.getMeeting(), session)
        );
    }

    private List<Stint> getStintsFallback(int driverNumber, Session session, Throwable t) {
        log.error("Fallback triggered for getStints(driverNumber={}, sessionKey={}): {}", driverNumber, session.getSessionKey(), t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getDriverRequestsFallback")
    @RateLimiter(name = RATE_LIMITER_NAME, fallbackMethod = "getDriverRequestsFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getDriverRequestsFallback")
    public List<DriverRequest> getDriverRequests(Session session) {
        log.debug("Fetching driver requests for session: {}", session.getSessionKey());
        return retrieveList(
                OpenF1Endpoints.drivers,
                Map.of("session_key", session.getSessionKey()),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private List<DriverRequest> getDriverRequestsFallback(Session session, Throwable t) {
        log.error("Fallback triggered for getDriverRequests(sessionKey={}): {}", session.getSessionKey(), t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getSessionFallback")
    @RateLimiter(name = RATE_LIMITER_NAME, fallbackMethod = "getSessionFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getSessionFallback")
    public SessionRequest getSession(int sessionKey) {
        log.debug("Fetching session for key: {}", sessionKey);
        return retrieveSingle(
                OpenF1Endpoints.sessions,
                Map.of("session_key", sessionKey),
                new ParameterizedTypeReference<>() {
                },
                "Session not found with key: " + sessionKey
        );
    }

    private SessionRequest getSessionFallback(int sessionKey, Throwable t) {
        log.error("Fallback triggered for getSession(sessionKey={}): {}", sessionKey, t.getMessage());
        throw new ResourceNotFoundException("Session not found with key: " + sessionKey);
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getLapsFallback")
    @RateLimiter(name = RATE_LIMITER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getLapsFallback")
    public List<LapRequest> getLaps(int sessionKey, int driverNumber) {
        log.debug("Fetching lap requests for session: {}, driver: {}", sessionKey, driverNumber);
        return retrieveList(
                OpenF1Endpoints.laps,
                Map.of("driver_number", driverNumber, "session_key", sessionKey),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private List<LapRequest> getLapsFallback(int sessionKey, int driverNumber, Throwable t) {
        log.error("Fallback triggered for getLaps(sessionKey={}, driverNumber={}): {}", sessionKey, driverNumber, t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getAllLapsFallback")
    @RateLimiter(name = RATE_LIMITER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getAllLapsFallback")
    public List<LapRequest> getAllLaps(int sessionKey) {
        log.debug("Fetching lap requests for session: {}", sessionKey);
        return retrieveList(
                OpenF1Endpoints.laps,
                Map.of("session_key", sessionKey),
                new ParameterizedTypeReference<>() {}
        );
    }

    private List<LapRequest> getAllLapsFallback(int sessionKey, Throwable t) {
        log.error("Fallback triggered for getLaps(sessionKey={}): {}", sessionKey, t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getCarDataFallback")
    @RateLimiter(name = RATE_LIMITER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getCarDataFallback")
    public List<CarDataRequest> getCarData(int driverNumber, Instant lapStart, Instant lapEnd, int sessionKey) {
        log.debug("Fetching car data for driver: {}, session: {}", driverNumber, sessionKey);
        Map<String, Object> params = new HashMap<>();
        params.put("driver_number", driverNumber);
        params.put("session_key", sessionKey);
        params.put("date>", lapStart);
        params.put("date<", lapEnd);
        return retrieveList(
                OpenF1Endpoints.car_data,
                params,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private List<CarDataRequest> getCarDataFallback(int driverNumber, Instant lapStart, Instant lapEnd, int sessionKey, Throwable t) {
        log.error("Fallback triggered for getCarData(driverNumber={}, sessionKey={}): {}", driverNumber, sessionKey, t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getAllCarDataFallback")
    @RateLimiter(name = RATE_LIMITER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getAllCarDataFallback")
    public List<CarDataRequest> getAllCarData(int driverNumber, int sessionKey) {
        log.debug("Fetching car data for driver: {}, session: {}", driverNumber, sessionKey);
        return retrieveList(
                OpenF1Endpoints.car_data,
                Map.of("driver_number", driverNumber, "session_key", sessionKey),
                new ParameterizedTypeReference<>() {}
        );
    }

    private List<CarDataRequest> getAllCarDataFallback(int driverNumber, int sessionKey, Throwable t) {
        log.error("Fallback triggered for getAllCarDataFallback(driverNumber={}, sessionKey={}): {})", driverNumber, sessionKey, t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getRaceControlEventsFallback")
    @RateLimiter(name = RATE_LIMITER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getRaceControlEventsFallback")
    public List<RaceControlEventRequest> getRaceControlEvents(int sessionKey) {
        log.debug("Fetching race control events for session: {}", sessionKey);
        Map<String, Object> params = new HashMap<>();
        params.put("session_key", sessionKey);
        return retrieveList(
                OpenF1Endpoints.race_control,
                params,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private List<RaceControlEventRequest> getRaceControlEventsFallback(int sessionKey, Throwable t) {
        log.error("Fallback triggered for getRaceControlEvents(sessionKey={}): {})", sessionKey, t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    @Retry(name = RETRY_NAME, fallbackMethod = "getLocationsFallback")
    @RateLimiter(name = RATE_LIMITER_NAME, fallbackMethod = "getLocationsFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getLocationsFallback")
    public List<LocationRequest> getLocations(int sessionKey, int driverNumber, Instant dateStart, Instant dateEnd) {
        log.debug("Fetching location requests for session: {}, driver: {}", sessionKey, driverNumber);
        Map<String, Object> params = new HashMap<>();
        params.put("session_key", sessionKey);
        params.put("driver_number", driverNumber);
        params.put("date>", dateStart);
        params.put("date<", dateEnd);
        return retrieveList(
                OpenF1Endpoints.location,
                params,
                new ParameterizedTypeReference<>() {}
        );
    }

    private List<LocationRequest> getLocationsFallback(int sessionKey, int driverNumber, Instant dateStart, Instant dateEnd, Throwable t) {
        log.error("Fallback triggered for getLocations(sessionKey={}, driverNumber={}): {}", sessionKey, driverNumber, t.getMessage());
        throwIfApiUnavailable(t);
        return Collections.emptyList();
    }

    private void throwIfApiUnavailable(Throwable t) {
        if (t instanceof CallNotPermittedException || t instanceof RequestNotPermitted) {
            throw new ServiceUnavailableException("OpenF1 API is currently unavailable");
        }
    }

    private <D, E> List<E> fetchAndMap(
            String endpoint,
            Map<String, Object> params,
            ParameterizedTypeReference<List<D>> typeRef,
            Function<D, E> mapper
    ) {
        List<D> dtos = retrieveList(endpoint, params, typeRef);
        return dtos.stream()
                .map(mapper)
                .toList();
    }

    private <T> List<T> retrieveList(
            String endpoint,
            Map<String, Object> params,
            ParameterizedTypeReference<List<T>> typeRef
    ) {
        List<T> result = retrieve(endpoint, params, typeRef);
        if (result == null || result.isEmpty()) {
            log.warn("No data returned from endpoint: {} with params: {}", endpoint, params);
            return Collections.emptyList();
        }
        return result;
    }

    private <T> T retrieveSingle(
            String endpoint,
            Map<String, Object> params,
            ParameterizedTypeReference<List<T>> typeRef,
            String errorMessage
    ) {
        List<T> result = retrieve(endpoint, params, typeRef);
        if (result == null || result.isEmpty()) {
            throw new ResourceNotFoundException(errorMessage);
        }
        return result.get(0);
    }

    private <T> T retrieve(String path, Map<String, Object> params, ParameterizedTypeReference<T> typeRef) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(path);
                        if (params != null) {
                            params.forEach((k, v) -> {
                                if (k.contains(">=") || k.contains("<=")) {
                                    uriBuilder.queryParam(k + v);
                                } else {
                                    if (v != null) uriBuilder.queryParam(k, v);
                                }
                            });
                        }
                        var uri = uriBuilder.build();
                        log.info("OpenF1 API Request: {}", uri);
                        return uri;
                    })
                    .retrieve()
                    .bodyToMono(typeRef)
                    .block();
        } catch (io.github.resilience4j.ratelimiter.RequestNotPermitted |
                 io.github.resilience4j.circuitbreaker.CallNotPermittedException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenF1 request failed for {}: {}", path, e.getMessage());
            throw e;
        }
    }
}