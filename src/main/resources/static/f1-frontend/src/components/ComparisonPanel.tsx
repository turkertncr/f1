import { useEffect, useState, useMemo, useCallback, useRef } from 'react';
import { motion } from 'framer-motion';
import { X, Trash2 } from 'lucide-react';
import { getCarData } from '../services/api';
import ComparativeTrackMap from './TrackDashboard';
import type { Driver, Lap, CarData } from '../types';

// ── Types ────────────────────────────────────────────────────────────────────
export interface ComparisonEntry {
    selectionId: string;
    driver: Driver;
    lap: Lap;
    color: string;
}

interface Props {
    sessionKey: string;
    entries: ComparisonEntry[];
    onRemoveEntry: (selectionId: string, lapNumber: number) => void;
    onClearAll: () => void;
    onColorChange: (selectionId: string, newColor: string) => void;
    readOnly?: boolean;
}

interface EntryCarData {
    carData: CarData[];
    loading: boolean;
}

// ── Telemetry Chart (moved from CarDataDashboard) ────────────────────────────
interface Series {
    id: string;
    data: number[];
    color: string;
    label: string;
}

interface ChartProps {
    series: Series[];
    label: string;
    unit?: string;
    min?: number;
    max?: number;
    boolean?: boolean;
    height?: number;
    hoveredPct?: number | null;
    onHover?: (pct: number | null) => void;
}

function TelemetryChart({ series, label, unit = '', min, max, boolean: isBool, height, hoveredPct, onHover }: ChartProps) {
    if (series.length === 0 || series.every(s => s.data.length === 0)) return null;

    let globalMin = min;
    let globalMax = max;

    if (globalMin === undefined || globalMax === undefined) {
        const allValues: number[] = [];
        series.forEach(s => allValues.push(...s.data));
        if (allValues.length > 0) {
            if (globalMin === undefined) globalMin = Math.min(...allValues);
            if (globalMax === undefined) globalMax = Math.max(...allValues);
        } else {
            globalMin = 0;
            globalMax = 100;
        }
    }

    const lo = globalMin;
    const hi = globalMax;
    const range = (hi - lo) || 1;

    const W = 800;
    const renderedH = isBool ? 48 : (height ?? 160);
    const H = isBool ? 60 : renderedH + 20;
    const pad = 8;

    return (
        <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="flex flex-col gap-2 p-4 md:p-5 bg-neutral-900/40 backdrop-blur-2xl rounded-3xl border border-neutral-800/60 shadow-2xl relative overflow-hidden group transition-all duration-500 hover:border-neutral-700/80"
        >
            <div className="absolute top-0 left-0 w-1/2 h-full opacity-[0.02] pointer-events-none bg-gradient-to-r from-white to-transparent" />
            <div className="flex items-center justify-between relative z-10">
                <span className="text-[11px] font-black uppercase tracking-widest text-neutral-400 drop-shadow-sm flex items-center gap-2.5">
                    {label}
                </span>
                <span className="font-mono text-xl font-black drop-shadow-md tracking-tight text-neutral-500">
                    {unit}
                </span>
            </div>

            <div className="relative bg-neutral-950/80 rounded-2xl overflow-hidden border border-neutral-800/80 shadow-inner mt-2">
                {!isBool && (
                    <div className="absolute inset-0 flex flex-col justify-between py-[8px] pointer-events-none opacity-20">
                        <div className="w-full h-px border-t border-dashed border-neutral-600" />
                        <div className="w-full h-px border-t border-dashed border-neutral-600" />
                        <div className="w-full h-px border-t border-dashed border-neutral-600" />
                    </div>
                )}

                <svg viewBox={`0 0 ${W} ${H}`} className="w-full drop-shadow-xl overflow-visible" style={{ height: renderedH }} preserveAspectRatio="none">
                    {series.map(s => {
                        if (s.data.length === 0) return null;
                        const pts = s.data.map((v, i) => {
                            const x = pad + (i / (s.data.length - 1)) * (W - pad * 2);
                            const y = H - pad - ((v - lo) / range) * (H - pad * 2);
                            return `${x},${y}`;
                        });
                        return (
                            <polyline
                                key={s.id}
                                points={pts.join(' ')}
                                fill="none"
                                stroke={s.color}
                                strokeWidth={1.5}
                                vectorEffect="non-scaling-stroke"
                                strokeLinejoin="round"
                                strokeLinecap="round"
                                className="drop-shadow-md"
                                style={{ filter: `drop-shadow(0px 4px 6px ${s.color}60)` }}
                            />
                        );
                    })}

                    {hoveredPct != null && (
                        <line
                            x1={pad + hoveredPct * (W - pad * 2)}
                            y1={0}
                            x2={pad + hoveredPct * (W - pad * 2)}
                            y2={H}
                            stroke="#737373"
                            strokeDasharray="3 3"
                            strokeWidth="1"
                        />
                    )}
                </svg>

                {hoveredPct != null && !isBool && series.map(s => {
                    if (s.data.length === 0) return null;
                    const idx = Math.round(hoveredPct * (s.data.length - 1));
                    const val = s.data[idx];
                    if (val == null) return null;
                    const xPct = ((pad + (idx / (s.data.length - 1)) * (W - pad * 2)) / W) * 100;
                    const yPct = ((H - pad - ((val - lo) / range) * (H - pad * 2)) / H) * 100;
                    return (
                        <div
                            key={s.id}
                            className="absolute z-10 pointer-events-none w-2 h-2 rounded-full border-2 bg-white"
                            style={{
                                left: `${xPct}%`,
                                top: `${yPct}%`,
                                transform: 'translate(-50%, -50%)',
                                borderColor: s.color,
                                boxShadow: `0 0 6px ${s.color}80`,
                            }}
                        />
                    );
                })}

                <div
                    className="absolute inset-0 z-20 cursor-crosshair"
                    onMouseMove={(e) => {
                        if (!onHover) return;
                        const rect = e.currentTarget.getBoundingClientRect();
                        const x = e.clientX - rect.left;
                        const pct = Math.max(0, Math.min(1, x / rect.width));
                        onHover(pct);
                    }}
                    onMouseLeave={() => onHover && onHover(null)}
                />

                {hoveredPct != null && (
                    <div
                        className="absolute pointer-events-none z-30 bg-neutral-800 border border-neutral-700 rounded-lg shadow-xl flex flex-col overflow-hidden"
                        style={{
                            left: `${hoveredPct * 100}%`,
                            top: '50%',
                            transform: `translate(${hoveredPct > 0.8 ? '-110%' : '15%'}, -50%)`
                        }}
                    >
                        <div className="bg-neutral-700/50 px-2 py-0.5 border-b border-neutral-700 flex justify-between">
                            <span className="text-[9px] font-bold text-neutral-300">{(hoveredPct * 100).toFixed(1)}% Lap</span>
                        </div>
                        <div className="px-2 py-1.5 flex flex-col gap-1.5">
                            {series.map(s => {
                                if (s.data.length === 0) return null;
                                const idx = Math.round(hoveredPct * (s.data.length - 1));
                                const val = s.data[idx];
                                return (
                                    <div key={s.id} className="flex items-center gap-2 justify-between">
                                        <div className="flex items-center gap-1.5">
                                            <div className="w-1.5 h-1.5 rounded-full shadow-sm" style={{ backgroundColor: s.color }} />
                                            <span className="text-[10px] text-neutral-400 font-bold">{s.label}</span>
                                        </div>
                                        <span className="text-xs font-mono font-bold text-white">
                                            {isBool ? (val ? 'ON' : 'OFF') : (val != null ? val.toFixed(1) : '')}{unit}
                                        </span>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}

                {!isBool && (
                    <div className="absolute inset-y-0 left-0 flex flex-col justify-between py-1.5 pl-2.5 pointer-events-none z-10">
                        <span className="text-[10px] text-neutral-300 font-mono font-bold bg-neutral-900/60 px-1.5 rounded backdrop-blur-sm border border-neutral-800/80">{hi}{unit}</span>
                        <span className="text-[10px] text-neutral-300 font-mono font-bold bg-neutral-900/60 px-1.5 rounded backdrop-blur-sm border border-neutral-800/80">{lo}{unit}</span>
                    </div>
                )}
            </div>
        </motion.div>
    );
}

// ── Sector Times Table ───────────────────────────────────────────────────────
function SectorTimesTable({ entries }: { entries: ComparisonEntry[] }) {
    const sectorCount = Math.max(...entries.map(e => e.lap.sectors?.length || 0), 0);
    if (sectorCount === 0) return null;

    const bestSectors: (number | null)[] = [];
    for (let i = 0; i < sectorCount; i++) {
        const times = entries.map(e => e.lap.sectors?.[i]).filter((t): t is number => t != null && t > 0);
        bestSectors.push(times.length > 0 ? Math.min(...times) : null);
    }

    const durations = entries.filter(e => e.lap.duration > 0).map(e => e.lap.duration);
    const bestTotal = durations.length > 0 ? Math.min(...durations) : null;

    const formatTime = (dur: number) => {
        const m = Math.floor(dur / 60);
        const s = (dur % 60).toFixed(3).padStart(6, '0');
        return `${m}:${s}`;
    };

    return (
        <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-neutral-900/40 backdrop-blur-2xl rounded-3xl border border-neutral-800/60 shadow-2xl overflow-hidden"
        >
            <div className="px-5 py-4">
                <span className="text-[11px] font-black uppercase tracking-widest text-neutral-400">Sector Times</span>
            </div>
            <div className="overflow-x-auto">
                <table className="w-full text-sm">
                    <thead>
                        <tr className="border-t border-neutral-800/60">
                            <th className="px-5 py-3 text-left text-[10px] font-black uppercase tracking-widest text-neutral-500">Driver</th>
                            <th className="px-4 py-3 text-center text-[10px] font-black uppercase tracking-widest text-neutral-500">Lap</th>
                            {Array.from({ length: sectorCount }).map((_, i) => (
                                <th key={i} className="px-4 py-3 text-center text-[10px] font-black uppercase tracking-widest text-neutral-500">S{i + 1}</th>
                            ))}
                            <th className="px-4 py-3 text-center text-[10px] font-black uppercase tracking-widest text-neutral-500">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        {entries.map((entry, idx) => (
                            <tr key={`${entry.selectionId}-${entry.lap.lap_number}`} className={`border-t border-neutral-800/40 ${idx % 2 === 0 ? 'bg-neutral-900/20' : ''}`}>
                                <td className="px-5 py-3">
                                    <div className="flex items-center gap-2.5">
                                        <div className="w-2 h-2 rounded-full" style={{ backgroundColor: entry.color }} />
                                        <span className="font-bold text-white text-sm">{entry.driver.name_acronym}</span>
                                    </div>
                                </td>
                                <td className="px-4 py-3 text-center font-mono font-bold text-neutral-300 text-sm">{entry.lap.lap_number}</td>
                                {Array.from({ length: sectorCount }).map((_, i) => {
                                    const sec = entry.lap.sectors?.[i];
                                    const isBest = sec != null && sec > 0 && bestSectors[i] != null && Math.abs(sec - bestSectors[i]!) < 0.0005;
                                    return (
                                        <td key={i} className="px-4 py-3 text-center">
                                            <span className={`font-mono font-bold text-sm ${isBest ? 'text-purple-400' : 'text-neutral-300'}`}>
                                                {sec != null && sec > 0 ? sec.toFixed(3) : '---'}
                                            </span>
                                        </td>
                                    );
                                })}
                                <td className="px-4 py-3 text-center">
                                    <span className={`font-mono font-bold text-sm ${entry.lap.duration > 0 && bestTotal != null && Math.abs(entry.lap.duration - bestTotal) < 0.0005 ? 'text-purple-400' : 'text-neutral-300'}`}>
                                        {entry.lap.duration > 0 ? formatTime(entry.lap.duration) : '---'}
                                    </span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </motion.div>
    );
}

// ── Main Comparison Panel ────────────────────────────────────────────────────
export default function ComparisonPanel({ sessionKey, entries, onRemoveEntry, onClearAll, onColorChange, readOnly = false }: Props) {
    const [dataMap, setDataMap] = useState<Record<string, EntryCarData>>({});
    const [hoveredPct, setHoveredPct] = useState<number | null>(null);
    const fetchedKeys = useRef<Set<string>>(new Set());
    const controllersRef = useRef<Map<string, AbortController>>(new Map());

    // Abort all in-flight requests only on true unmount
    useEffect(() => {
        const controllers = controllersRef.current;
        const keys = fetchedKeys.current;
        return () => {
            controllers.forEach(c => c.abort());
            controllers.clear();
            keys.clear();
        };
    }, []);

    // Fetch car data for each entry (no cleanup — avoids aborting on dep changes)
    useEffect(() => {
        entries.forEach(entry => {
            const key = `${entry.selectionId}-${entry.lap.lap_number}`;
            if (fetchedKeys.current.has(key)) return;
            fetchedKeys.current.add(key);

            setDataMap(prev => ({ ...prev, [key]: { carData: [], loading: true } }));

            const controller = new AbortController();
            controllersRef.current.set(key, controller);

            getCarData(parseInt(sessionKey), entry.driver.driver_number, entry.lap.lap_number, controller.signal)
                .then(data => {
                    setDataMap(prev => ({ ...prev, [key]: { carData: data, loading: false } }));
                })
                .catch(() => {
                    fetchedKeys.current.delete(key);
                    setDataMap(prev => {
                        const next = { ...prev };
                        delete next[key];
                        return next;
                    });
                })
                .finally(() => {
                    controllersRef.current.delete(key);
                });
        });

        // Clean up stale keys (entries that were removed)
        const activeKeys = new Set(entries.map(e => `${e.selectionId}-${e.lap.lap_number}`));
        fetchedKeys.current.forEach(k => {
            if (!activeKeys.has(k)) {
                fetchedKeys.current.delete(k);
                const ctrl = controllersRef.current.get(k);
                if (ctrl) { ctrl.abort(); controllersRef.current.delete(k); }
                setDataMap(prev => {
                    const next = { ...prev };
                    delete next[k];
                    return next;
                });
            }
        });
    }, [entries, sessionKey]);

    // Derive chart series
    const validEntries = useMemo(() => {
        return entries.filter(e => {
            const key = `${e.selectionId}-${e.lap.lap_number}`;
            return dataMap[key]?.carData.length > 0;
        });
    }, [entries, dataMap]);

    const makeLabel = useCallback((e: ComparisonEntry) => `${e.driver.name_acronym} L${e.lap.lap_number}`, []);

    const speedSeries = useMemo(() => validEntries.map(e => ({
        id: `${e.selectionId}-${e.lap.lap_number}`, color: e.color, label: makeLabel(e),
        data: dataMap[`${e.selectionId}-${e.lap.lap_number}`].carData.map(d => d.speed ?? 0)
    })), [validEntries, dataMap, makeLabel]);

    const throttleSeries = useMemo(() => validEntries.map(e => ({
        id: `${e.selectionId}-${e.lap.lap_number}`, color: e.color, label: makeLabel(e),
        data: dataMap[`${e.selectionId}-${e.lap.lap_number}`].carData.map(d => d.throttle ?? 0)
    })), [validEntries, dataMap, makeLabel]);

    const gearSeries = useMemo(() => validEntries.map(e => ({
        id: `${e.selectionId}-${e.lap.lap_number}`, color: e.color, label: makeLabel(e),
        data: dataMap[`${e.selectionId}-${e.lap.lap_number}`].carData.map(d => d.gear ?? 0)
    })), [validEntries, dataMap, makeLabel]);

    const brakeSeries = useMemo(() => validEntries.map(e => ({
        id: `${e.selectionId}-${e.lap.lap_number}`, color: e.color, label: makeLabel(e),
        data: dataMap[`${e.selectionId}-${e.lap.lap_number}`].carData.map(d => d.brake ? 1 : 0)
    })), [validEntries, dataMap, makeLabel]);

    const driversForMap = useMemo(() => validEntries.map(e => ({
        driver_number: e.driver.driver_number,
        color: e.color,
        speeds: dataMap[`${e.selectionId}-${e.lap.lap_number}`].carData.map(d => d.speed ?? 0)
    })), [validEntries, dataMap]);

    const isLoading = entries.some(e => {
        const key = `${e.selectionId}-${e.lap.lap_number}`;
        return dataMap[key]?.loading;
    });

    return (
        <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
            className="mt-8 flex flex-col gap-6"
        >
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="inline-flex items-center gap-2 bg-purple-600/10 border border-purple-600/20 rounded-full px-3 py-1">
                        <span className="text-purple-400 font-bold tracking-wider text-xs uppercase">Comparison Analysis</span>
                    </div>
                    {isLoading && (
                        <div className="w-4 h-4 border-2 border-purple-500 border-t-transparent rounded-full animate-spin" />
                    )}
                </div>
                {!readOnly && (
                    <button
                        onClick={onClearAll}
                        className="flex items-center gap-1.5 px-3 py-1.5 rounded-full border border-neutral-700 bg-neutral-900 text-neutral-400 hover:text-red-400 hover:border-red-500/30 transition-colors text-xs font-bold uppercase tracking-widest"
                    >
                        <Trash2 className="w-3 h-3" /> Clear All
                    </button>
                )}
            </div>

            {/* Selected lap tags */}
            <div className="flex flex-wrap gap-2">
                {entries.map(entry => (
                    <motion.div
                        key={`${entry.selectionId}-${entry.lap.lap_number}`}
                        initial={{ opacity: 0, scale: 0.9 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.9 }}
                        className="flex items-center gap-2 bg-neutral-900/60 border border-neutral-800 rounded-full px-3 py-1.5 group"
                    >
                        {readOnly ? (
                            <div className="flex items-center justify-center w-3.5 h-3.5 shrink-0 rounded-full ring-1 ring-white/10">
                                <div className="w-full h-full rounded-full" style={{ backgroundColor: entry.color }} />
                            </div>
                        ) : (
                            <label className="cursor-pointer relative flex items-center justify-center w-3.5 h-3.5 shrink-0 rounded-full ring-1 ring-white/10 hover:ring-white/30 transition-all" title="Change color">
                                <div className="w-full h-full rounded-full" style={{ backgroundColor: entry.color }} />
                                <input
                                    type="color"
                                    value={entry.color}
                                    onChange={e => onColorChange(entry.selectionId, e.target.value)}
                                    className="absolute opacity-0 w-full h-full cursor-pointer"
                                />
                            </label>
                        )}
                        <span className="text-xs font-bold text-white">{entry.driver.name_acronym}</span>
                        <span className="text-xs font-mono text-neutral-400">L{entry.lap.lap_number}</span>
                        {!readOnly && (
                            <button
                                onClick={() => onRemoveEntry(entry.selectionId, entry.lap.lap_number)}
                                className="ml-0.5 p-0.5 text-neutral-600 hover:text-red-400 transition-colors"
                            >
                                <X className="w-3 h-3" />
                            </button>
                        )}
                    </motion.div>
                ))}
            </div>

            {/* Sector Times */}
            <SectorTimesTable entries={entries} />

            {/* Speed Dominance Map + Telemetry */}
            <div className="flex flex-col gap-6">
                {/* Track Map — top right, above the charts */}
                <div className="w-full lg:w-1/2 xl:w-5/12 lg:self-end bg-neutral-900/40 border border-neutral-800 rounded-3xl p-6 relative flex flex-col min-h-[400px]">
                    <div className="flex items-center gap-2 mb-4">
                        <span className="text-xs font-black uppercase tracking-widest text-neutral-400 drop-shadow-sm flex items-center gap-2">
                            Speed Dominance Map
                        </span>
                    </div>
                    <div className="flex-1 w-full h-full min-h-[300px] relative flex items-center justify-center bg-neutral-950/50 rounded-2xl border border-neutral-800/50 shadow-inner p-4">
                        {validEntries.length > 0 ? (
                            <ComparativeTrackMap
                                sessionKey={parseInt(sessionKey)}
                                baseDriverNumber={validEntries[0].driver.driver_number}
                                baseLapNumber={validEntries[0].lap.lap_number}
                                driversData={driversForMap}
                            />
                        ) : (
                            <div className="flex flex-col items-center justify-center gap-2">
                                <div className="w-5 h-5 border-2 border-neutral-500 border-t-transparent rounded-full animate-spin" />
                                <span className="text-neutral-600 text-xs font-bold uppercase tracking-widest text-center">Loading telemetry data…</span>
                            </div>
                        )}
                    </div>
                </div>

                {/* Telemetry Charts — full width */}
                <div className="w-full flex flex-col gap-5">
                    <TelemetryChart series={speedSeries} label="Speed" min={0} max={380} height={480} hoveredPct={hoveredPct} onHover={setHoveredPct} />
                    <TelemetryChart series={throttleSeries} label="Throttle" min={0} max={100} hoveredPct={hoveredPct} onHover={setHoveredPct} />
                    <TelemetryChart series={gearSeries} label="Gear" min={0} max={8} hoveredPct={hoveredPct} onHover={setHoveredPct} />
                    <TelemetryChart series={brakeSeries} label="Brake" min={0} max={1} boolean hoveredPct={hoveredPct} onHover={setHoveredPct} />
                </div>
            </div>
        </motion.div>
    );
}
