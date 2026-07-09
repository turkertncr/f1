import axios from 'axios';
import type { Meeting, Session, Result, Driver, Stint, CarData, Lap, Location, Pace } from '../types';

// Backend API URL
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

const backendApi = axios.create({
  baseURL: BACKEND_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ===== BACKEND API CALLS =====

/**
 * Fetch all meetings for a given year from your backend
 */
export const getMeetings = async (year: number, signal?: AbortSignal): Promise<Meeting[]> => {
  const response = await backendApi.get<Meeting[]>(`/meetings?year=${year}`, {signal});
  return response.data;
};

/**
 * Fetch all sessions for a given meeting from your backend
 */
export const getSessions = async (meetingKey: number, signal?: AbortSignal): Promise<Session[]> => {
  const response = await backendApi.get<Session[]>(`/sessions?meetingKey=${meetingKey}`, {signal});
  return response.data;
};

/**
 * Fetch all results for a given session from your backend
 */
export const getResults = async (sessionKey: number, signal?: AbortSignal): Promise<Result[]> => {
  const response = await backendApi.get<Result[]>(`/results?sessionKey=${sessionKey}`, {signal});
  return response.data;
};

// ===== OPENF1 API CALLS =====

/**
 * Fetch drivers from OpenF1 API
 */
export const getDrivers = async (sessionKey: string, signal?: AbortSignal): Promise<Driver[]> => {
  const response = await backendApi.get<Driver[]>(`/drivers?sessionKey=${sessionKey}`, {signal});
  return response.data;
};

/**
 * Fetch driver stints for a session
 */
export const getDriverStints = async (driverNumber: number, sessionKey: number, signal?: AbortSignal): Promise<Stint[]> => {
  const response = await backendApi.get<Stint[]>(`/stints?driverNumber=${driverNumber}&sessionKey=${sessionKey}`, {signal});
  return response.data;
};

/**
 * Fetch laps for a driver in a session
 */
export const getLaps = async (sessionKey: number, driverNumber: number, signal?: AbortSignal): Promise<Lap[]> => {
  const response = await backendApi.get<Lap[]>(`/laps?sessionKey=${sessionKey}&driverNumber=${driverNumber}`, {signal});
  return response.data;
};

/**
 * Fetch car telemetry data for a driver on a specific lap
 */
export const getCarData = async (sessionKey: number, driverNumber: number, lapNumber: number, signal?: AbortSignal): Promise<CarData[]> => {
  const response = await backendApi.get<CarData[]>(`/car_data?sessionKey=${sessionKey}&driverNumber=${driverNumber}&lapNumber=${lapNumber}`, {signal});
  return response.data;
};

/**
 * Fetch location data for a driver in specific lap of a session
 */
export const getLocation = async (sessionKey: number, driverNumber: number, lapNumber: number, signal?: AbortSignal): Promise<Location[]> => {
  const response = await backendApi.get<Location[]>(`/locations?sessionKey=${sessionKey}&driverNumber=${driverNumber}&lapNumber=${lapNumber}`, {signal});
  return response.data;
}

/**
 * Fetch race pace data for all drivers in a session
 */
export const getPace = async (sessionKey: number, signal?: AbortSignal): Promise<Pace[]> => {
  const response = await backendApi.get<Pace[]>(`/laps/pace?sessionKey=${sessionKey}`, { signal });
  return response.data;
};

