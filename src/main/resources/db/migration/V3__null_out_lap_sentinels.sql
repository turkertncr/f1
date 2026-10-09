-- OpenF1 omits date_start / lap_duration on some laps (out laps, deleted laps, red-flagged runs).
-- Those gaps used to be persisted as 1970-01-01 and 0, which are indistinguishable from real data:
-- a 0 duration survives the outlier filter and drags pace averages down, and a 1970 lap_start makes
-- the car-data and location time-window lookups query an empty range. Both columns are nullable,
-- so restore the unknowns to NULL.
--
-- No lap has a 0s duration, and OpenF1 coverage starts in 2018, so neither predicate can match a
-- genuine value.

update lap set duration = null where duration = 0;

update lap set lap_start = null where lap_start < timestamptz '2018-01-01 00:00:00+00';
