import { useEffect, useState, useMemo, useCallback, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowLeft, Activity, Zap, ChevronDown, Plus, X, BarChart3, Maximize2, Contrast } from 'lucide-react';
import { getDrivers, getLaps, getPace, getDriverStints } from '../services/api';
import ComparisonPanel from './ComparisonPanel';
import type { ComparisonEntry } from './ComparisonPanel';
import type { Driver, Lap, Pace, Stint } from '../types';

interface Props {
    sessionKey: string;
    meetingName: string;
    sessionName: string;
    onBack: () => void;
}

// ── Shared Types ─────────────────────────────────────────────────────────────
export interface FetchState {
    laps: Lap[];
    loadingLaps: boolean;
    error: string | null;
}

export interface Selection {
    id: string;
    driver: Driver | null;
    color: string;
}

const MAX_COMPARISON_LAPS = 6;

// ── Multi-series Lap Time Chart ──────────────────────────────────────────────
interface LapTimeSeries {
    id: string;
    driver: Driver;
    laps: Lap[];
    color: string;
}

interface LapTimeChartProps {
    series: LapTimeSeries[];
    selectedLaps: Record<string, number[]>;
    onToggleLap: (id: string, lap: Lap) => void;
    totalSelected: number;
}

function LapTimeChart({ series, selectedLaps, onToggleLap, totalSelected }: LapTimeChartProps) {
    const [hideOutliers, setHideOutliers] = useState(false);

    const allValidLaps = useMemo(() => {
        return series.flatMap(s => s.laps.filter(l => l.duration > 0));
    }, [series]);

    if (allValidLaps.length === 0) return null;

    const minDur = Math.min(...allValidLaps.map(l => l.duration));
    const cutoff = minDur * 1.5;

    const chartSeries = useMemo(() => {
        return series.map(s => ({
            ...s,
            laps: s.laps.map(l => ({
                ...l,
                isValid: (!hideOutliers || !l.outlier) && l.duration > 0 && l.duration <= (hideOutliers ? minDur * 1.07 : cutoff)
            }))
        }));
    }, [series, cutoff, hideOutliers]);

    const validDurations = chartSeries.flatMap(s => s.laps.filter(l => l.isValid).map(l => l.duration));
    const lo = validDurations.length ? Math.min(...validDurations) : 0;
    const hi = validDurations.length ? Math.max(...validDurations) : 100;
    const range = (hi - lo) || 1;
    const yMin = lo - range * 0.1;
    const yMax = hi + range * 0.1;
    const yRange = yMax - yMin;

    const H = 280;
    const padX = 60;
    const padY = 30;

    const allLapNums = allValidLaps.map(l => l.lap_number);
    const minLap = Math.min(...allLapNums);
    const maxLap = Math.max(...allLapNums);
    const xRange = Math.max(maxLap - minLap, 1);

    // Dynamically calculate width to spread points out, ensuring a minimum distance of 28px per lap
    const W = Math.max(1000, padX * 2 + xRange * 28);

    const formatTime = (dur: number) => {
        const m = Math.floor(dur / 60);
        const s = (dur % 60).toFixed(3).padStart(6, '0');
        return `${m}:${s}`;
    };

    return (
        <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="w-full bg-neutral-900/60 backdrop-blur-2xl border border-neutral-800/80 rounded-3xl p-6 shadow-2xl relative mb-8 group"
        >
            <div className="flex justify-between items-center mb-6">
                <div className="flex items-center gap-3">
                    <h3 className="text-sm font-black uppercase tracking-widest text-neutral-300 drop-shadow-sm">Lap Times</h3>
                </div>
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => setHideOutliers(h => !h)}
                        className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full border transition-colors text-xs font-medium ${hideOutliers ? 'bg-red-500/20 text-red-400 border-red-500/30' : 'bg-neutral-900 text-neutral-500 border-neutral-700 hover:bg-neutral-800'}`}
                    >
                        <span className="uppercase tracking-widest">{hideOutliers ? 'Outliers Hidden' : 'Hide Outliers'}</span>
                    </button>
                    <div className="text-xs font-medium text-neutral-500 bg-neutral-950/50 px-3 py-1.5 rounded-full border border-neutral-800/50">
                        Click laps to compare ({totalSelected}/{MAX_COMPARISON_LAPS})
                    </div>
                </div>
            </div>

            <div className="relative w-full overflow-x-auto overflow-y-hidden bg-neutral-950/80 rounded-2xl border border-neutral-800/60 shadow-inner">
                <svg viewBox={`0 0 ${W} ${H}`} style={{ minWidth: W }} className="h-full drop-shadow-xl overflow-visible cursor-crosshair">
                    {[0, 0.25, 0.5, 0.75, 1].map(pct => {
                        const y = H - padY - pct * (H - padY * 2);
                        const dur = yMin + pct * yRange;
                        return (
                            <g key={pct}>
                                <line x1={padX} y1={y} x2={W - padX} y2={y} stroke="#333" strokeDasharray="4 4" strokeWidth="1" opacity={0.5} />
                                <text x={padX - 12} y={y + 3} fill="#777" fontSize="10" textAnchor="end" fontFamily="monospace" fontWeight="bold">{formatTime(dur)}</text>
                            </g>
                        );
                    })}

                    {Array.from({ length: maxLap - minLap + 1 }).map((_, i) => {
                        const lapNum = minLap + i;
                        const x = padX + ((lapNum - minLap) / xRange) * (W - padX * 2);
                        const isMajor = lapNum % 5 === 0 || lapNum === 1 || lapNum === maxLap;
                        return (
                            <g key={lapNum}>
                                <line x1={x} y1={H - padY} x2={x} y2={H - padY + (isMajor ? 6 : 3)} stroke="#555" strokeWidth="1" />
                                {isMajor && <text x={x} y={H - padY + 18} fill="#777" fontSize="10" textAnchor="middle" fontFamily="monospace" fontWeight="bold">{lapNum}</text>}
                            </g>
                        );
                    })}

                    {chartSeries.map(s => {
                        let paths: string[] = [];
                        let currentPath: string[] = [];
                        s.laps.forEach(l => {
                            if (l.isValid) {
                                const x = padX + ((l.lap_number - minLap) / xRange) * (W - padX * 2);
                                const y = H - padY - ((l.duration - yMin) / yRange) * (H - padY * 2);
                                currentPath.push(`${x},${y}`);
                            } else {
                                if (currentPath.length > 0) {
                                    paths.push(currentPath.join(' '));
                                    currentPath = [];
                                }
                            }
                        });
                        if (currentPath.length > 0) paths.push(currentPath.join(' '));

                        const selectedSet = new Set(selectedLaps[s.id] || []);

                        return (
                            <g key={s.id}>
                                {paths.map((pts, i) => (
                                    <polyline key={i} points={pts} fill="none" stroke={s.color} strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" style={{ filter: `drop-shadow(0px 4px 6px ${s.color}60)` }} />
                                ))}
                                {s.laps.map(l => {
                                    if (!l.isValid) return null;
                                    const x = padX + ((l.lap_number - minLap) / xRange) * (W - padX * 2);
                                    const y = H - padY - ((l.duration - yMin) / yRange) * (H - padY * 2);
                                    const isSelected = selectedSet.has(l.lap_number);
                                    const isPit = l.is_pit_lap;
                                    const canSelect = isSelected || totalSelected < MAX_COMPARISON_LAPS;

                                    return (
                                        <g
                                            key={l.lap_number}
                                            onClick={() => canSelect && onToggleLap(s.id, l)}
                                            className={canSelect ? 'cursor-crosshair' : 'cursor-not-allowed'}
                                            style={{ pointerEvents: 'all' as any }}
                                        >
                                            <circle cx={x} cy={y} r="6" fill="transparent" />
                                            <circle
                                                cx={x}
                                                cy={y}
                                                r={isSelected ? 6 : 4}
                                                fill={isSelected ? '#fff' : (isPit ? '#555' : '#111')}
                                                stroke={isSelected ? s.color : (isPit ? '#999' : s.color)}
                                                strokeWidth={isSelected ? 3 : 2}
                                                className="transition-all duration-300 hover:stroke-white hover:fill-white"
                                                style={isSelected ? { filter: `drop-shadow(0px 0px 8px ${s.color})` } : undefined}
                                            />
                                            <title>{s.driver.name_acronym} Lap {l.lap_number} {isPit ? '(PIT)' : ''}: {formatTime(l.duration)}{isSelected ? ' ✓' : ''}</title>
                                        </g>
                                    );
                                })}
                            </g>
                        );
                    })}
                </svg>
                <div className="absolute left-2 top-1/2 -translate-y-1/2 -rotate-90 text-[10px] font-black uppercase tracking-widest text-neutral-500 origin-center pointer-events-none">Lap Time</div>
                <div className="absolute bottom-1 right-2 text-[10px] font-black uppercase tracking-widest text-neutral-500 pointer-events-none">Lap Number</div>
            </div>
        </motion.div>
    );
}

// ── Race Pace Chart ──────────────────────────────────────────────────────────
function RacePaceChart({ paceData, loading }: { paceData: Pace[]; loading: boolean }) {
    const [hoveredDriver, setHoveredDriver] = useState<number | null>(null);

    if (loading) {
        return (
            <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="w-full bg-neutral-900/60 backdrop-blur-2xl border border-neutral-800/80 rounded-3xl p-6 shadow-2xl mb-8"
            >
                <h3 className="text-sm font-black uppercase tracking-widest text-neutral-300 mb-4">Race Pace</h3>
                <div className="flex items-center justify-center py-12 gap-2">
                    <div className="w-4 h-4 border-2 border-neutral-500 border-t-transparent rounded-full animate-spin" />
                    <span className="text-neutral-500 text-xs uppercase tracking-widest font-bold">Loading pace data…</span>
                </div>
            </motion.div>
        );
    }

    // Filter out drivers with 0 pace and sort ascending (fastest first)
    const sorted = [...paceData].filter(p => p.pace > 0).sort((a, b) => a.pace - b.pace);
    if (sorted.length === 0) return null;

    const bestPace = sorted[0].pace;
    const worstPace = sorted[sorted.length - 1].pace;
    const range = worstPace - bestPace || 1;

    const formatTime = (dur: number) => {
        const m = Math.floor(dur / 60);
        const s = (dur % 60).toFixed(3).padStart(6, '0');
        return `${m}:${s}`;
    };

    return (
        <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="w-full bg-neutral-900/60 backdrop-blur-2xl border border-neutral-800/80 rounded-3xl p-6 shadow-2xl relative mb-8"
        >
            <div className="flex justify-between items-center mb-5">
                <div className="flex items-center gap-3">
                    <h3 className="text-sm font-black uppercase tracking-widest text-neutral-300 drop-shadow-sm">Race Pace</h3>
                    <span className="text-[10px] font-mono text-neutral-600 bg-neutral-800/50 px-2 py-0.5 rounded-full border border-neutral-700/50">Avg Lap Time (excl. outliers)</span>
                </div>
            </div>

            <div className="flex flex-col gap-[3px]">
                {sorted.map((entry, index) => {
                    const teamColor = `#${entry.driver.team_colour || '555'}`;
                    // Inverted: slowest = longest bar, fastest = shortest
                    const delta = entry.pace - bestPace;
                    const barPct = Math.max(25, 30 + (delta / range) * 70);
                    const isHovered = hoveredDriver === entry.driver.driver_number;

                    return (
                        <motion.div
                            key={entry.driver.driver_number}
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: index * 0.02, duration: 0.3 }}
                            className="flex items-center gap-0 relative"
                            onMouseEnter={() => setHoveredDriver(entry.driver.driver_number)}
                            onMouseLeave={() => setHoveredDriver(null)}
                        >
                            {/* Driver name */}
                            <span className="w-16 text-right text-[11px] font-bold text-neutral-300 shrink-0 pr-2 truncate">
                                {entry.driver.name_acronym}
                            </span>

                            {/* Bar with time inside */}
                            <div className="flex-1 h-[25px] relative">
                                <motion.div
                                    initial={{ width: 0 }}
                                    animate={{ width: `${barPct}%` }}
                                    transition={{ delay: index * 0.02 + 0.05, duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
                                    className="h-full relative overflow-hidden cursor-pointer"
                                    style={{ backgroundColor: teamColor }}
                                >
                                    {/* Time label inside bar */}
                                    <span className="absolute left-2 top-1/2 -translate-y-1/2 text-[11px] font-mono font-black text-white whitespace-nowrap"
                                        style={{ textShadow: '0 1px 3px rgba(0,0,0,0.5)' }}
                                    >
                                        {formatTime(entry.pace)}
                                    </span>
                                </motion.div>

                                {/* Hover tooltip */}
                                {isHovered && (
                                    <div
                                        className="absolute z-50 bg-neutral-800 border border-neutral-700 rounded-lg shadow-xl px-3 py-2 pointer-events-none"
                                        style={{
                                            left: `calc(${barPct}% + 8px)`,
                                            top: '50%',
                                            transform: 'translateY(-50%)',
                                        }}
                                    >
                                        <div className="text-[11px] font-bold text-white whitespace-nowrap">{entry.driver.full_name}</div>
                                        <div className="flex items-center gap-1.5 mt-0.5">
                                            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: teamColor }} />
                                            <span className="text-[10px] text-neutral-400 font-medium">Pace:</span>
                                            <span className="text-[11px] font-mono font-bold text-white">{formatTime(entry.pace)}</span>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </motion.div>
                    );
                })}
            </div>
        </motion.div>
    );
}

function DriverPicker({ drivers, selected, onSelect, disabledNumbers }: { drivers: Driver[]; selected: Driver | null; onSelect: (d: Driver) => void; disabledNumbers?: Set<number> }) {
    const [open, setOpen] = useState(false);
    return (
        <div className="relative z-50 w-full">
            <button onClick={() => setOpen(o => !o)} className="flex items-center justify-between w-full gap-2 bg-neutral-900/80 hover:bg-neutral-800 border border-neutral-700/80 hover:border-neutral-500 rounded-lg px-3 py-2 transition-all shadow-inner group">
                {selected ? (
                    <span className="font-bold text-white text-sm transition-colors truncate">{selected.full_name}</span>
                ) : (
                    <span className="text-neutral-500 text-sm font-medium">Select driver…</span>
                )}
                <ChevronDown className={`w-3.5 h-3.5 shrink-0 text-neutral-500 transition-transform ${open ? 'rotate-180' : ''}`} />
            </button>
            <AnimatePresence>
                {open && (
                    <motion.div initial={{ opacity: 0, y: -8, scale: 0.97 }} animate={{ opacity: 1, y: 0, scale: 1 }} exit={{ opacity: 0, y: -8, scale: 0.97 }} transition={{ duration: 0.15 }} className="absolute top-full mt-2 left-0 w-64 bg-neutral-900 border border-neutral-700 rounded-xl overflow-hidden shadow-2xl max-h-72 overflow-y-auto">
                        {drivers.map(d => {
                            const isDisabled = disabledNumbers?.has(d.driver_number) ?? false;
                            return (
                                <button
                                    key={d.driver_number}
                                    onClick={() => { if (!isDisabled) { onSelect(d); setOpen(false); } }}
                                    disabled={isDisabled}
                                    className={`w-full flex items-center gap-3 px-4 py-2.5 transition-colors text-left ${isDisabled ? 'opacity-30 cursor-not-allowed' : 'hover:bg-neutral-800 cursor-pointer'}`}
                                >
                                    <div className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: `#${d.team_colour || '555'}` }} />
                                    <span className="font-bold text-white text-sm">{d.full_name}</span>
                                    <span className="text-neutral-500 text-xs ml-auto">#{d.driver_number}</span>
                                </button>
                            );
                        })}
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}

// ── Compound color config ────────────────────────────────────────────────────
const compoundConfig: Record<string, {
    color: string;
    darkText: boolean;
    label: string;
    fullLabel: string;
}> = {
    SOFT:         { color: '#e8002d', darkText: false, label: 'S', fullLabel: 'Soft' },
    MEDIUM:       { color: '#ffd700', darkText: true,  label: 'M', fullLabel: 'Medium' },
    HARD:         { color: '#d9d9d9', darkText: true,  label: 'H', fullLabel: 'Hard' },
    INTERMEDIATE: { color: '#39b54a', darkText: false, label: 'I', fullLabel: 'Inter' },
    WET:          { color: '#0067ff', darkText: false, label: 'W', fullLabel: 'Wet' },
};

// ── Stint cache types ────────────────────────────────────────────────────────
interface StintCacheEntry {
    stints: Stint[];
    loading: boolean;
}
type StintCache = Record<number, StintCacheEntry>;

// ── Inline Stints (pure render, data supplied via cache) ─────────────────────
function InlineStints({ stints, loading }: { stints: Stint[]; loading: boolean }) {
    if (loading) {
        return (
            <div className="flex items-center gap-1.5 px-3">
                <div className="w-3 h-3 border-2 border-neutral-600 border-t-transparent rounded-full animate-spin" />
                <span className="text-[9px] text-neutral-600 uppercase tracking-widest font-bold">Stints</span>
            </div>
        );
    }

    if (stints.length === 0) return null;

    const totalLaps = Math.max(...stints.map(s => s.lap_end));

    return (
        <div className="flex items-center gap-2 min-w-0">
            <span className="text-[9px] text-neutral-600 uppercase tracking-widest font-bold shrink-0">Stints</span>
            <div className="flex gap-[2px] h-[28px] rounded-md overflow-hidden flex-1 min-w-[120px]">
                {stints.map((stint, index) => {
                    const cfg = compoundConfig[stint.compound] || compoundConfig.MEDIUM;
                    const widthPct = ((stint.lap_end - stint.lap_start + 1) / totalLaps) * 100;
                    const laps = stint.lap_end - stint.lap_start + 1;
                    const textAlpha = cfg.darkText ? 'rgba(0,0,0,0.72)' : 'rgba(255,255,255,0.92)';
                    const subAlpha = cfg.darkText ? 'rgba(0,0,0,0.45)' : 'rgba(255,255,255,0.55)';
                    const badgeBg = cfg.darkText ? 'rgba(0,0,0,0.12)' : 'rgba(0,0,0,0.22)';
                    const badgeBorder = cfg.darkText ? 'rgba(0,0,0,0.18)' : 'rgba(255,255,255,0.22)';

                    return (
                        <motion.div
                            key={stint.stint_number}
                            initial={{ opacity: 0, scaleX: 0 }}
                            animate={{ opacity: 1, scaleX: 1 }}
                            transition={{ delay: index * 0.05, duration: 0.3, ease: [0.34, 1.56, 0.64, 1] }}
                            style={{
                                width: `${widthPct}%`,
                                minWidth: 24,
                                backgroundColor: cfg.color,
                                transformOrigin: 'left',
                            }}
                            className="relative flex items-center justify-center gap-1 overflow-hidden cursor-default"
                            title={`${cfg.fullLabel} · Laps ${stint.lap_start}–${stint.lap_end}${stint.tyre_age > 0 ? ` · +${stint.tyre_age} laps old` : ''}`}
                        >
                            {/* Depth layers */}
                            <div className="absolute inset-x-0 top-0 h-2/5 bg-gradient-to-b from-white/20 to-transparent pointer-events-none" />
                            <div className="absolute inset-x-0 bottom-0 h-1/3 bg-gradient-to-t from-black/20 to-transparent pointer-events-none" />

                            {/* Tyre badge */}
                            <div
                                className="relative z-10 w-[16px] h-[16px] rounded-full flex items-center justify-center shrink-0"
                                style={{
                                    backgroundColor: badgeBg,
                                    border: `1px solid ${badgeBorder}`,
                                    boxShadow: 'inset 0 1px 2px rgba(0,0,0,0.15)',
                                }}
                            >
                                <span className="text-[9px] font-black leading-none" style={{ color: textAlpha }}>
                                    {cfg.label}
                                </span>
                            </div>

                            {/* Lap count */}
                            {widthPct > 8 && (
                                <span className="relative z-10 text-[8px] font-bold leading-none tabular-nums" style={{ color: subAlpha }}>
                                    {laps}L
                                </span>
                            )}
                        </motion.div>
                    );
                })}
            </div>
        </div>
    );
}

// ── SelectionItem Component ──────────────────────────────────────────────────
interface SelectionItemProps {
    sessionKey: string;
    drivers: Driver[];
    selection: Selection;
    onUpdate: (selection: Selection) => void;
    onRemove: () => void;
    canRemove: boolean;
    onDataLoaded: (id: string, data: FetchState) => void;
    zIndex: number;
    usedDriverNumbers: Set<number>;
    onSelectFastestLap: (selectionId: string) => void;
    stintCache: StintCache;
}

function SelectionItem({ sessionKey, drivers, selection, onUpdate, onRemove, canRemove, onDataLoaded, zIndex, usedDriverNumbers, onSelectFastestLap, stintCache }: SelectionItemProps) {
    const [state, setState] = useState<FetchState>({
        laps: [], loadingLaps: false, error: null
    });

    const updateState = useCallback((update: Partial<FetchState>) => {
        setState(prev => ({ ...prev, ...update }));
    }, []);

    useEffect(() => {
        onDataLoaded(selection.id, state);
    }, [state, selection.id, onDataLoaded]);

    useEffect(() => {
        if (!selection.driver) return;
        const controller = new AbortController();
        updateState({ loadingLaps: true, error: null });
        getLaps(parseInt(sessionKey), selection.driver!.driver_number, controller.signal)
            .then(data => {
                updateState({ laps: data.filter(l => l.duration && l.duration > 0), loadingLaps: false });
            })
            .catch(() => { if (!controller.signal.aborted) updateState({ error: 'Failed to load laps', loadingLaps: false }); });

        return () => controller.abort();
    }, [selection.driver, sessionKey, updateState]);

    const bestLap = state.laps.length
        ? state.laps.reduce((best, lap) => lap.duration > 0 && lap.duration < best.duration ? lap : best)
        : null;

    const formatTime = (dur: number | undefined | null) => {
        if (!dur) return '--:--.---';
        const m = Math.floor(dur / 60);
        const s = (dur % 60).toFixed(3).padStart(6, '0');
        return `${m}:${s}`;
    };

    const teamColor = selection.driver ? `#${selection.driver.team_colour || '555'}` : '#555';

    return (
        <div className="flex bg-neutral-900/40 border border-neutral-800 rounded-xl relative group p-3 pr-2 items-center" style={{ zIndex }}>
            <div className="absolute top-0 left-0 bottom-0 w-1 rounded-l-xl" style={{ backgroundColor: teamColor }} />

            {/* Driver Picker */}
            <div className="flex flex-col justify-between gap-3 pl-3 w-56 shrink-0 py-1 pr-4">
                <div className="flex items-center gap-2 w-full">
                    <div className="flex-1 min-w-0">
                        <DriverPicker
                            drivers={drivers}
                            selected={selection.driver}
                            disabledNumbers={usedDriverNumbers}
                            onSelect={d => {
                                let hex = d.team_colour || '555555';
                                if (hex.length === 3) hex = hex.split('').map(c => c + c).join('');
                                else if (hex.length !== 6) hex = '555555';
                                onUpdate({ ...selection, driver: d, color: `#${hex}` });
                            }}
                        />
                    </div>
                </div>
            </div>

            {/* Best Lap Info + Fastest Lap Button */}
            <div className="flex items-center gap-4 pl-6 pr-6 shrink-0 py-1">
                <div className="flex flex-col items-start justify-center gap-0.5 h-full">
                    <span className="text-[9px] text-neutral-500 font-black uppercase tracking-wider flex items-center gap-1"><Zap className="w-3 h-3 text-yellow-500" />Best Lap</span>
                    <span className="text-sm font-mono font-bold text-white">{formatTime(bestLap?.duration)}</span>
                </div>
                {/* Thunder button to select fastest lap */}
                {selection.driver && bestLap && !state.loadingLaps && (
                    <button
                        onClick={() => onSelectFastestLap(selection.id)}
                        title="Select fastest lap for comparison"
                        className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-yellow-500/10 hover:bg-yellow-500/25 border border-yellow-500/30 hover:border-yellow-400/50 text-yellow-400 hover:text-yellow-300 transition-all duration-200 group/zap"
                    >
                        <Zap className="w-3.5 h-3.5 fill-current group-hover/zap:animate-pulse" />
                        <span className="text-[10px] font-bold uppercase tracking-wider">Fastest</span>
                    </button>
                )}
                {state.loadingLaps && (
                    <div className="w-4 h-4 border-2 border-neutral-500 border-t-transparent rounded-full animate-spin" />
                )}
            </div>

            {/* Divider */}
            {selection.driver && (
                <div className="w-px h-8 bg-neutral-800 shrink-0 mx-1" />
            )}

            {/* Inline Stints */}
            {selection.driver && (
                <div className="flex-1 min-w-0 px-3 pr-8">
                    <InlineStints
                        stints={stintCache[selection.driver.driver_number]?.stints ?? []}
                        loading={stintCache[selection.driver.driver_number]?.loading ?? true}
                    />
                </div>
            )}

            {canRemove && (
                <button onClick={onRemove} className="absolute right-3 top-1/2 -translate-y-1/2 p-1.5 text-neutral-600 hover:text-red-400 rounded-lg transition-colors">
                    <X className="w-4 h-4" />
                </button>
            )}
        </div>
    );
}

// ── Main Dashboard ───────────────────────────────────────────────────────────
export default function CarDataDashboard({ sessionKey, meetingName, sessionName, onBack }: Props) {
    const [drivers, setDrivers] = useState<Driver[]>([]);
    const [loadingDrivers, setLoadingDrivers] = useState(true);

    const [paceData, setPaceData] = useState<Pace[]>([]);
    const [loadingPace, setLoadingPace] = useState(true);

    const [selections, setSelections] = useState<Selection[]>([{ id: '1', driver: null, color: '#e50000' }]);
    const [fetchedData, setFetchedData] = useState<Record<string, FetchState>>({});

    // Stint cache: driver_number -> { stints, loading }
    const [stintCache, setStintCache] = useState<StintCache>({});
    const stintFetchedRef = useRef<Set<number>>(new Set());

    // Multi-lap comparison selection: selectionId -> lap numbers
    const [comparisonLaps, setComparisonLaps] = useState<Record<string, number[]>>({});

    useEffect(() => {
        const controller = new AbortController();
        setLoadingDrivers(true);
        getDrivers(sessionKey, controller.signal)
            .then(data => setDrivers(data.sort((a, b) => a.driver_number - b.driver_number)))
            .catch(() => { })
            .finally(() => { if (!controller.signal.aborted) setLoadingDrivers(false); });
        return () => controller.abort();
    }, [sessionKey]);

    // Fetch pace data
    useEffect(() => {
        const controller = new AbortController();
        setLoadingPace(true);
        getPace(parseInt(sessionKey), controller.signal)
            .then(data => setPaceData(data))
            .catch(() => {})
            .finally(() => { if (!controller.signal.aborted) setLoadingPace(false); });
        return () => controller.abort();
    }, [sessionKey]);

    const handleDataLoaded = useCallback((id: string, data: FetchState) => {
        setFetchedData(prev => ({ ...prev, [id]: data }));
    }, []);

    // Fetch stints for any newly-selected driver (cached, no re-fetch)
    useEffect(() => {
        const driverNums = selections
            .filter(s => s.driver)
            .map(s => s.driver!.driver_number)
            .filter(num => !stintFetchedRef.current.has(num));

        if (driverNums.length === 0) return;

        const controllers: AbortController[] = [];

        for (const num of driverNums) {
            stintFetchedRef.current.add(num);
            setStintCache(prev => ({ ...prev, [num]: { stints: [], loading: true } }));

            const controller = new AbortController();
            controllers.push(controller);

            getDriverStints(num, parseInt(sessionKey), controller.signal)
                .then(data => {
                    setStintCache(prev => ({ ...prev, [num]: { stints: data, loading: false } }));
                })
                .catch(() => {
                    if (!controller.signal.aborted) {
                        setStintCache(prev => ({ ...prev, [num]: { stints: [], loading: false } }));
                    }
                });
        }

        return () => controllers.forEach(c => c.abort());
    }, [selections, sessionKey]);

    const addSelection = () => {
        setSelections(prev => [...prev, { id: Date.now().toString(), driver: null, color: '#e50000' }]);
    };

    const updateSelection = useCallback((sel: Selection) => {
        setSelections(prev => prev.map(s => s.id === sel.id ? sel : s));
    }, []);

    const removeSelection = (id: string) => {
        setSelections(prev => prev.filter(s => s.id !== id));
        setFetchedData(prev => {
            const next = { ...prev };
            delete next[id];
            return next;
        });
        setComparisonLaps(prev => {
            const next = { ...prev };
            delete next[id];
            return next;
        });
    };

    // Total selected laps across all drivers
    const totalSelected = useMemo(() => {
        return Object.values(comparisonLaps).reduce((sum, arr) => sum + arr.length, 0);
    }, [comparisonLaps]);

    // Toggle a lap in/out of comparison
    const handleToggleLap = useCallback((id: string, lap: Lap) => {
        setComparisonLaps(prev => {
            const current = prev[id] || [];
            const exists = current.includes(lap.lap_number);

            if (exists) {
                const filtered = current.filter(n => n !== lap.lap_number);
                return { ...prev, [id]: filtered };
            }

            const total = Object.values(prev).reduce((sum, arr) => sum + arr.length, 0);
            if (total >= MAX_COMPARISON_LAPS) return prev;

            return { ...prev, [id]: [...current, lap.lap_number] };
        });
    }, []);

    // Build lap time chart series
    const lapTimeSeries = selections.filter(s => s.driver && fetchedData[s.id]?.laps.length > 0).map(s => ({
        id: s.id,
        driver: s.driver!,
        laps: fetchedData[s.id].laps,
        color: s.color
    }));

    // Build comparison entries for ComparisonPanel
    const comparisonEntries: ComparisonEntry[] = useMemo(() => {
        const entries: ComparisonEntry[] = [];
        for (const sel of selections) {
            if (!sel.driver) continue;
            const lapNums = comparisonLaps[sel.id] || [];
            const fetchState = fetchedData[sel.id];
            if (!fetchState) continue;
            for (const lapNum of lapNums) {
                const lap = fetchState.laps.find(l => l.lap_number === lapNum);
                if (lap) {
                    entries.push({
                        selectionId: sel.id,
                        driver: sel.driver,
                        lap,
                        color: sel.color,
                    });
                }
            }
        }
        return entries;
    }, [selections, comparisonLaps, fetchedData]);

    const handleRemoveComparisonEntry = useCallback((selectionId: string, lapNumber: number) => {
        setComparisonLaps(prev => ({
            ...prev,
            [selectionId]: (prev[selectionId] || []).filter(n => n !== lapNumber)
        }));
    }, []);

    const handleClearAllComparisons = useCallback(() => {
        setComparisonLaps({});
    }, []);

    // Set of driver numbers already in use (for deduplication)
    const usedDriverNumbers = useMemo(() => {
        return new Set(selections.filter(s => s.driver).map(s => s.driver!.driver_number));
    }, [selections]);

    // Handle color change from ComparisonPanel (updates the selection's color)
    const handleColorChange = useCallback((selectionId: string, newColor: string) => {
        setSelections(prev => prev.map(s => s.id === selectionId ? { ...s, color: newColor } : s));
    }, []);

    // Select fastest lap for a driver
    const handleSelectFastestLap = useCallback((selectionId: string) => {
        const fetchState = fetchedData[selectionId];
        if (!fetchState || fetchState.laps.length === 0) return;
        const validLaps = fetchState.laps.filter(l => l.duration > 0);
        if (validLaps.length === 0) return;
        const fastest = validLaps.reduce((best, lap) => lap.duration < best.duration ? lap : best);

        setComparisonLaps(prev => {
            const current = prev[selectionId] || [];
            // Already selected? Skip
            if (current.includes(fastest.lap_number)) return prev;
            const total = Object.values(prev).reduce((sum, arr) => sum + arr.length, 0);
            if (total >= MAX_COMPARISON_LAPS) return prev;
            return { ...prev, [selectionId]: [...current, fastest.lap_number] };
        });
    }, [fetchedData]);

    // Telemetry popup state
    const [showTelemetryPopup, setShowTelemetryPopup] = useState(false);
    const [highContrast, setHighContrast] = useState(false);
    // Snapshot of entries frozen at the moment the popup opens (static/read-only)
    const [snapshotEntries, setSnapshotEntries] = useState<ComparisonEntry[]>([]);

    const openTelemetryPopup = useCallback(() => {
        // Freeze the current entries so the popup is static
        setSnapshotEntries([...comparisonEntries]);
        setShowTelemetryPopup(true);
    }, [comparisonEntries]);

    const closeTelemetryPopup = useCallback(() => {
        setShowTelemetryPopup(false);
        setSnapshotEntries([]);
    }, []);

    // ── BroadcastChannel sync with comparison page ─────────────────────────────
    const channelRef = useRef<BroadcastChannel | null>(null);

    useEffect(() => {
        const channel = new BroadcastChannel('f1-comparison');
        channelRef.current = channel;

        channel.onmessage = (event) => {
            const { type, payload } = event.data;
            if (type === 'remove-entry') {
                handleRemoveComparisonEntry(payload.selectionId, payload.lapNumber);
            } else if (type === 'clear-all') {
                handleClearAllComparisons();
            } else if (type === 'color-change') {
                handleColorChange(payload.selectionId, payload.newColor);
            } else if (type === 'request-data') {
                // Comparison page just opened, send current data
                channel.postMessage({
                    type: 'update',
                    payload: { sessionKey, entries: comparisonEntries }
                });
            }
        };

        return () => channel.close();
    }, [sessionKey, comparisonEntries, handleRemoveComparisonEntry, handleClearAllComparisons, handleColorChange]);

    // Broadcast entries whenever they change
    useEffect(() => {
        channelRef.current?.postMessage({
            type: 'update',
            payload: { sessionKey, entries: comparisonEntries }
        });
    }, [comparisonEntries, sessionKey]);

    return (
        <div className="p-4 md:p-8 max-w-[1400px] mx-auto">
            <motion.div initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }} className="mb-8">
                <button onClick={onBack} className="text-sm text-neutral-500 hover:text-white mb-4 flex items-center gap-2 transition-colors uppercase tracking-wider font-bold group">
                    <ArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" /> Back to Results
                </button>
                <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
                    <div>
                        <div className="inline-flex items-center gap-2 bg-indigo-600/10 border border-indigo-600/20 rounded-full px-3 py-1 mb-3">
                            <Activity className="w-3 h-3 text-indigo-400" />
                            <span className="text-indigo-400 font-bold tracking-wider text-xs uppercase">Telemetry Comparison</span>
                        </div>
                        <h1 className="text-3xl md:text-4xl font-black tracking-tight uppercase">{meetingName}</h1>
                        <p className="text-neutral-500 mt-1">{sessionName} · Driver Telemetry Comparison</p>
                    </div>

                </div>
            </motion.div>

            {/* Driver Selectors - Full Width */}
            <div className="flex flex-col gap-4 mb-8">
                {loadingDrivers ? (
                    <div className="h-20 w-full bg-neutral-900 rounded-xl animate-pulse border border-neutral-800" />
                ) : (
                    <>
                        {selections.map((sel, index) => (
                            <SelectionItem
                                key={sel.id}
                                sessionKey={sessionKey}
                                drivers={drivers}
                                selection={sel}
                                onUpdate={updateSelection}
                                onRemove={() => removeSelection(sel.id)}
                                canRemove={selections.length > 1}
                                onDataLoaded={handleDataLoaded}
                                zIndex={selections.length - index}
                                usedDriverNumbers={usedDriverNumbers}
                                onSelectFastestLap={handleSelectFastestLap}
                                stintCache={stintCache}
                            />
                        ))}
                        <button
                            onClick={addSelection}
                            className="self-start flex items-center gap-2 bg-neutral-900/50 hover:bg-neutral-800 border border-neutral-800 text-neutral-400 px-4 py-2.5 rounded-xl transition-colors font-bold text-sm"
                        >
                            <Plus className="w-4 h-4" /> Add Driver
                        </button>
                    </>
                )}
            </div>

            {/* Lap Time Chart */}
            {lapTimeSeries.length > 0 && (
                <LapTimeChart
                    series={lapTimeSeries}
                    selectedLaps={comparisonLaps}
                    onToggleLap={handleToggleLap}
                    totalSelected={totalSelected}
                />
            )}

            {/* Race Pace Chart */}
            <RacePaceChart paceData={paceData} loading={loadingPace} />

            {/* Selected laps summary + Visualize button */}
            {comparisonEntries.length > 0 && (
                <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="mt-6 bg-neutral-900/40 border border-neutral-800 rounded-2xl p-4"
                >
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <div className="inline-flex items-center gap-2 bg-purple-600/10 border border-purple-600/20 rounded-full px-3 py-1">
                                <span className="text-purple-400 font-bold tracking-wider text-xs uppercase">Selected Laps</span>
                            </div>
                            <span className="text-neutral-500 text-xs font-mono">{comparisonEntries.length}/{MAX_COMPARISON_LAPS}</span>
                        </div>
                        <div className="flex items-center gap-3">
                            <button
                                onClick={handleClearAllComparisons}
                                className="text-[10px] font-bold uppercase tracking-widest text-neutral-500 hover:text-red-400 transition-colors"
                            >
                                Clear All
                            </button>
                        </div>
                    </div>

                    {/* Lap tags */}
                    <div className="flex flex-wrap gap-1.5 mt-3">
                        {comparisonEntries.map(entry => (
                            <div
                                key={`${entry.selectionId}-${entry.lap.lap_number}`}
                                className="flex items-center gap-1.5 bg-neutral-800/60 border border-neutral-700/50 rounded-full px-2.5 py-1"
                            >
                                <div className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: entry.color }} />
                                <span className="text-[10px] font-bold text-white">{entry.driver.name_acronym}</span>
                                <span className="text-[10px] font-mono text-neutral-400">L{entry.lap.lap_number}</span>
                                <button
                                    onClick={() => handleRemoveComparisonEntry(entry.selectionId, entry.lap.lap_number)}
                                    className="p-0.5 text-neutral-600 hover:text-red-400 transition-colors"
                                >
                                    <X className="w-2.5 h-2.5" />
                                </button>
                            </div>
                        ))}
                    </div>

                    {/* Visualize Telemetry button */}
                    <div className="mt-4 flex justify-center">
                        <button
                            onClick={openTelemetryPopup}
                            className="flex items-center gap-2.5 px-6 py-3 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-bold text-sm uppercase tracking-wider shadow-lg shadow-purple-600/20 hover:shadow-purple-500/30 transition-all duration-300 hover:scale-[1.02] active:scale-[0.98]"
                        >
                            <BarChart3 className="w-4.5 h-4.5" />
                            Visualize Telemetry
                        </button>
                    </div>
                </motion.div>
            )}

            {/* Telemetry Popup Modal */}
            <AnimatePresence>
                {showTelemetryPopup && (
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        transition={{ duration: 0.2 }}
                        className="fixed inset-0 z-[9999] flex items-center justify-center"
                    >
                        {/* Backdrop */}
                        <div
                            className="absolute inset-0 bg-black/80 backdrop-blur-sm"
                            onClick={closeTelemetryPopup}
                        />

                        {/* Modal */}
                        <motion.div
                            initial={{ opacity: 0, scale: 0.92, y: 20 }}
                            animate={{ opacity: 1, scale: 1, y: 0 }}
                            exit={{ opacity: 0, scale: 0.92, y: 20 }}
                            transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
                            className="relative z-10 w-[95vw] h-[90vh] max-w-[1600px] bg-neutral-950 border border-neutral-800 rounded-2xl overflow-hidden shadow-2xl shadow-black/50 flex flex-col"
                        >
                            {/* Popup Header */}
                            <div className="flex items-center justify-between px-6 py-3.5 border-b border-neutral-800/80 bg-neutral-950/90 backdrop-blur-xl shrink-0">
                                <div className="flex items-center gap-3">
                                    <BarChart3 className="w-4.5 h-4.5 text-purple-400" />
                                    <h2 className="text-sm font-black uppercase tracking-wider text-white">Telemetry Analysis</h2>
                                </div>
                                <div className="flex items-center gap-2">
                                    <button
                                        onClick={() => setHighContrast(hc => !hc)}
                                        className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-bold uppercase tracking-wider transition-all duration-200 ${
                                            highContrast
                                                ? 'bg-blue-500/20 text-blue-400 border-blue-500/40'
                                                : 'bg-neutral-900 text-neutral-400 border-neutral-700 hover:bg-neutral-800 hover:text-neutral-300'
                                        }`}
                                    >
                                        <Contrast className="w-3.5 h-3.5" />
                                        High Contrast {highContrast ? 'On' : 'Off'}
                                    </button>
                                    <button
                                        onClick={() => window.open('/comparison', '_blank')}
                                        className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-neutral-700 bg-neutral-900 text-neutral-400 hover:bg-neutral-800 hover:text-neutral-300 text-xs font-bold uppercase tracking-wider transition-all duration-200"
                                    >
                                        <Maximize2 className="w-3.5 h-3.5" />
                                        Open Fullscreen
                                    </button>
                                    <button
                                        onClick={closeTelemetryPopup}
                                        className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-red-600/20 border border-red-600/30 text-red-400 hover:bg-red-600/30 hover:text-red-300 text-xs font-bold uppercase tracking-wider transition-all duration-200"
                                    >
                                        <X className="w-3.5 h-3.5" />
                                        Close
                                    </button>
                                </div>
                            </div>

                            {/* Static read-only ComparisonPanel */}
                            <div className={`flex-1 overflow-y-auto p-4 md:p-8 ${highContrast ? 'contrast-125 brightness-110' : ''}`}>
                                <ComparisonPanel
                                    sessionKey={sessionKey}
                                    entries={snapshotEntries}
                                    onRemoveEntry={() => {}}
                                    onClearAll={() => {}}
                                    onColorChange={() => {}}
                                    readOnly
                                />
                            </div>
                        </motion.div>
                    </motion.div>
                )}
            </AnimatePresence>

            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.5 }} className="mt-12 text-center text-neutral-700 text-xs pb-12">
                <p>Telemetry powered by OpenF1 API</p>
            </motion.div>
        </div>
    );
}
