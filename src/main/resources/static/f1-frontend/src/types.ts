// Backend Response DTOs
export interface Meeting {
  meeting_key: number;
  meeting_name: string;
  country_name: string;
  location: string;
}

export interface Session {
  session_key: number;
  session_name: string;
  session_type: string;
}

export interface Driver {
  driver_number: number;
  broadcast_name: string;
  full_name: string;
  name_acronym: string;
  team_name: string;
  team_colour: string;
  headshot_url: string;
  country_code: string;
}

export interface Result {
  dnf: boolean;
  dns: boolean;
  dsq: boolean;
  driver_number: number;
  driver: Driver;
  duration: number[] | null;
  gap_to_leader: string[] | null;
  laps: number | null;
  position: number;
}

export interface Stint {
  stint_number: number;
  lap_start: number;
  lap_end: number;
  compound: 'SOFT' | 'MEDIUM' | 'HARD' | 'INTERMEDIATE' | 'WET' | 'TEST_UNKNOWN' | 'UNKNOWN';
  tyre_age: number;
  driver_number: number;
}

export interface Lap {
  id: number;
  session_id: number;
  driver_number: number;
  lap_start: string;
  lap_number: number;
  duration: number;
  is_pit_lap: boolean;
  outlier: boolean;
  sectors: number[];
  avg_speed: number;
  top_speed: number;
}

export interface CarData {
  id: number;
  session_id: number;
  driver_number: number;
  date: string;
  brake: boolean;
  speed: number;
  gear: number;
  throttle: number;
  drs: number;
}

export interface Location {
  driver_number: number;
  x: number;
  y: number;
}

export interface Pace {
  driver: Driver;
  pace: number;
}
