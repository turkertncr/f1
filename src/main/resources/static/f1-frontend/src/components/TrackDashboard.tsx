import { useEffect, useState } from "react";
import { getLocation } from "../services/api.ts";
import type { Location } from "../types.ts";

export interface DriverTrackData {
  driver_number: number;
  color: string;
  speeds: number[]; // Array of speeds from CarData
}

interface Props {
  sessionKey: number;
  baseDriverNumber: number;
  baseLapNumber: number;
  driversData: DriverTrackData[];
}

export default function ComparativeTrackMap({ sessionKey, baseDriverNumber, baseLapNumber, driversData }: Props) {
  const [points, setPoints] = useState<Location[]>([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    getLocation(sessionKey, baseDriverNumber, baseLapNumber, controller.signal)
        .then(data => { setPoints(data); setError(false); })
        .catch(() => { if (!controller.signal.aborted) setError(true); });
    return () => controller.abort();
  }, [sessionKey, baseDriverNumber, baseLapNumber]);

  if (error) return (
      <div className="flex flex-col items-center justify-center h-full text-neutral-500 gap-3">
          <span className="text-xs font-bold uppercase tracking-widest">No track data for this lap</span>
      </div>
  );

  if (points.length === 0 || driversData.length === 0) return (
      <div className="flex flex-col items-center justify-center h-full text-neutral-500 gap-3">
          <div className="w-5 h-5 border-2 border-neutral-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-xs font-bold uppercase tracking-widest">Loading Track Geometry</span>
      </div>
  );

  const xs = points.map(p => p.x);
  const ys = points.map(p => p.y);
  const minX = Math.min(...xs), maxX = Math.max(...xs);
  const minY = Math.min(...ys), maxY = Math.max(...ys);
  const pad = 650; // Adding padding for track edges

  // OpenF1 coordinates are Y-up but SVG is Y-down, so flip vertically:
  // paths are drawn in raw coordinates inside a scale(1,-1) group, which maps
  // the Y range [minY, maxY] to [-maxY, -minY].
  const viewBox = `${minX - pad} ${-maxY - pad} ${maxX - minX + pad * 2} ${maxY - minY + pad * 2}`;

  // Filter out any drivers that don't have speeds to prevent errors
  const validDrivers = driversData.filter(d => d.speeds.length > 0);

  // We will build an array of path segments. Each segment has a color.
  const segments: { path: string, color: string }[] = [];

  if (validDrivers.length === 0) {
      // Fallback
      let d = `M ${points[0].x} ${points[0].y}`;
      for (let i = 1; i < points.length; i++) d += ` L ${points[i].x} ${points[i].y}`;
      segments.push({ path: d, color: '#555' });
  } else {
      let currentPath = `M ${points[0].x} ${points[0].y}`;
      let currentColor = validDrivers[0].color;

      const N = points.length;

      for (let i = 1; i < N; i++) {
          const progress = i / (N - 1);

          // Find the fastest driver at this progress point
          let maxSpeed = -1;
          let fastestColor = '#555';

          for (const driver of validDrivers) {
              const speedIndex = Math.min(
                  driver.speeds.length - 1,
                  Math.round(progress * (driver.speeds.length - 1))
              );
              const speed = driver.speeds[speedIndex] ?? 0;
              if (speed > maxSpeed) {
                  maxSpeed = speed;
                  fastestColor = driver.color;
              }
          }

          const dist = Math.hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y);
          if (dist < 4000) {
              if (fastestColor !== currentColor) {
                  segments.push({ path: currentPath, color: currentColor });
                  currentPath = `M ${points[i-1].x} ${points[i-1].y} L ${points[i].x} ${points[i].y}`;
                  currentColor = fastestColor;
              } else {
                  currentPath += ` L ${points[i].x} ${points[i].y}`;
              }
          } else {
              segments.push({ path: currentPath, color: currentColor });
              currentPath = `M ${points[i].x} ${points[i].y}`;
          }
      }
      segments.push({ path: currentPath, color: currentColor });
  }

  return (
      <svg viewBox={viewBox} className="w-full h-full max-h-[300px] drop-shadow-2xl">
          <g transform="scale(1, -1)">
              {segments.map((seg, idx) => (
                  <path
                      key={idx}
                      d={seg.path}
                      fill="none"
                      stroke={seg.color}
                      strokeWidth={450}
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      className="transition-all duration-360"
                  />
              ))}
          </g>
      </svg>
  );
}