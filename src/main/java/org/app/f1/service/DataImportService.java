package org.app.f1.service;

import lombok.RequiredArgsConstructor;
import org.app.f1.entities.*;
import org.app.f1.repositories.GenericRepo;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataImportService {

    private final GenericRepo genericRepo;

    public void saveAllMeetings(List<Meeting> list) {
        String sql = """
                insert into meetings (meeting_key, circuit_key, name, official_name, location,
                country_name, country_code, circuit_name, date_start, date_end, year)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (meeting_key) do nothing""";
        genericRepo.batchInsertIgnore(sql, list, (ps, meeting) -> {
            ps.setInt(1, meeting.getMeetingKey());
            ps.setInt(2, meeting.getCircuitKey());
            ps.setString(3, meeting.getName());
            ps.setString(4, meeting.getOfficialName());
            ps.setString(5, meeting.getLocation());
            ps.setString(6, meeting.getCountryName());
            ps.setString(7, meeting.getCountryCode());
            ps.setString(8, meeting.getCircuitName());
            ps.setObject(9, meeting.getDateStart().atOffset(ZoneOffset.UTC));
            ps.setObject(10, meeting.getDateEnd().atOffset(ZoneOffset.UTC));
            ps.setInt(11, meeting.getYear());
        });
    }

    public void saveAllSessions(List<Session> list) {
        String sql = """
                insert into race_session (session_key, meeting_id, date_start, date_end, type, session_name)
                values (?, ?, ?, ?, ?, ?)
                on conflict (session_key) do nothing""";
        genericRepo.batchInsertIgnore(sql, list, (ps, session) -> {
            ps.setInt(1, session.getSessionKey());
            ps.setObject(2, session.getMeeting().getId());
            ps.setObject(3, session.getDateStart().atOffset(ZoneOffset.UTC));
            ps.setObject(4, session.getDateEnd().atOffset(ZoneOffset.UTC));
            ps.setString(5, session.getType().name());
            ps.setString(6, session.getSessionName());
        });
    }

    public void saveAllStints(List<Stint> list) {
        String sql = """
                insert into stints (lap_start, lap_end, stint_number, tyre_age_at_start,
                session_id, meeting_id, driver_number, compound)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (stint_number, session_id, driver_number) do nothing""";
        genericRepo.batchInsertIgnore(sql, list, (ps, stint) -> {
            ps.setObject(1, stint.getLapStart());
            ps.setObject(2, stint.getLapEnd());
            ps.setObject(3, stint.getStintNumber());
            ps.setObject(4, stint.getTyreAge());
            ps.setObject(5, stint.getSession().getId());
            ps.setObject(6, stint.getMeeting().getId());
            ps.setObject(7, stint.getDriverNumber());
            ps.setString(8, stint.getCompound() != null ? stint.getCompound().name() : null);
        });
    }

    public void saveAllLaps(List<Lap> list) {
        String sql = """
                insert into lap (session_id, driver_number, lap_start, lap_number, duration, is_pit_lap, outlier)
                values (?, ?, ?, ?, ?, ?, ?)
                on conflict (session_id, driver_number, lap_number) do nothing""";
        genericRepo.batchInsertIgnore(sql, list, (ps, lap) -> {
            ps.setObject(1, lap.getSession().getId());
            ps.setObject(2, lap.getDriverNumber());
            if (lap.getLapStart() != null) {
                ps.setObject(3, lap.getLapStart().atOffset(ZoneOffset.UTC));
            } else {
                ps.setNull(3, Types.TIMESTAMP_WITH_TIMEZONE);
            }
            ps.setObject(4, lap.getLapNumber());
            if (lap.getDuration() != null) {
                ps.setDouble(5, lap.getDuration());
            } else {
                ps.setNull(5, Types.DOUBLE);
            }
            ps.setBoolean(6, lap.isPitLap());
            ps.setBoolean(7, lap.isOutlier());
        });
    }

    public void saveAllResults(List<Result> list) {
        String sql = """
                insert into results (dnf, dns, dsq, driver_number, duration, gap_to_leader,
                laps, meeting_key, position, session_key, session_id)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (session_id, driver_number) do nothing""";
        genericRepo.batchInsertIgnore(sql, list, (ps, result) -> {
            ps.setObject(1, result.getDnf());
            ps.setObject(2, result.getDns());
            ps.setObject(3, result.getDsq());
            ps.setObject(4, result.getDriverNumber());
            if (result.getDuration() != null) {
                ps.setArray(5, ps.getConnection().createArrayOf("float8", result.getDuration().toArray()));
            } else {
                ps.setNull(5, Types.ARRAY);
            }
            if (result.getGapToLeader() != null) {
                ps.setArray(6, ps.getConnection().createArrayOf("text", result.getGapToLeader().toArray()));
            } else {
                ps.setNull(6, Types.ARRAY);
            }
            ps.setObject(7, result.getLaps());
            ps.setObject(8, result.getMeetingKey());
            ps.setObject(9, result.getPosition());
            ps.setObject(10, result.getSessionKey());
            ps.setObject(11, result.getSession().getId());
        });
    }

    public void saveAllCarData(List<CarData> list) {
        String sql = """
                insert into car_data (brake, speed, gear, throttle, drs, driver_number, session_id, date, lap_number)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (driver_number, session_id, date) do nothing""";
        genericRepo.batchInsertIgnore(sql, list, (ps, car) -> {
            ps.setObject(1, car.getBrake());
            ps.setObject(2, car.getSpeed());
            ps.setObject(3, car.getGear());
            ps.setObject(4, car.getThrottle());
            ps.setObject(5, car.getDrs());
            ps.setObject(6, car.getDriverNumber());
            ps.setObject(7, car.getSession().getId());
            ps.setObject(8, car.getDate().atOffset(ZoneOffset.UTC));
            ps.setObject(9, car.getLapNumber());
        });
    }

    public void saveAllLocations(List<Location> list) {
        String sql = """
                insert into locations (session_id, driver_number, date, x, y)
                values (?, ?, ?, ?, ?)
                on conflict (date, driver_number) do nothing""";
        genericRepo.batchInsertIgnore(sql, list, (ps, location) -> {
            ps.setObject(1, location.getSession().getId());
            ps.setObject(2, location.getDriverNumber());
            ps.setObject(3, location.getDate().atOffset(ZoneOffset.UTC));
            ps.setObject(4, location.getX());
            ps.setObject(5, location.getY());
        });
    }

    public void saveAllSectors(List<Sector> list) {
        String sql = """
              insert into sector (lap_id, sector, time, segments)
              values (?, ?, ?, ?)
              on conflict (lap_id, sector) do nothing""";
        genericRepo.batchInsertIgnore(sql, list, (ps, sector) -> {
            ps.setObject(1, sector.getLap().getId());
            ps.setInt(2, sector.getSector());
            ps.setObject(3, sector.getTime());
            List<Integer> segments = sector.getSegments();
            if (segments != null && !segments.isEmpty()) {
                ps.setArray(4, ps.getConnection().createArrayOf("int4",
                        segments.toArray()));
            } else {
                ps.setNull(4, Types.ARRAY);
            }
        });
    }

}
