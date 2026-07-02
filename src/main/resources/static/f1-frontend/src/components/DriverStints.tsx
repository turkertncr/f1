import { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown, Loader2 } from 'lucide-react';
import { getDriverStints } from '../services/api';
import type { Stint, Driver } from '../types';

interface Props {
    driver: Driver;
    sessionKey: number;
    totalLaps?: number;
}

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

export default function DriverStints({ driver, sessionKey, totalLaps = 70 }: Props) {
    const [stints, setStints] = useState<Stint[]>([]);
    const [loading, setLoading] = useState(false);
    const [expanded, setExpanded] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!expanded) return;

        const controller = new AbortController();

        const fetchStints = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await getDriverStints(driver.driver_number, sessionKey, controller.signal);
                setStints(data);
            } catch (err: unknown) {
                if (err instanceof Error && err.name !== 'AbortError' && err.name !== 'CanceledError') {
                    setError('Failed to load stint data');
                }
            } finally {
                if (!controller.signal.aborted) setLoading(false);
            }
        };

        fetchStints();
        return () => controller.abort();
    }, [expanded, driver.driver_number, sessionKey]);

    const actualTotalLaps = stints.length > 0
        ? Math.max(...stints.map(s => s.lap_end), totalLaps)
        : totalLaps;

    return (
        <div className="mt-2">
            {/* Toggle Button */}
            <button
                onClick={() => setExpanded(!expanded)}
                className="w-full flex items-center gap-2.5 py-2 px-3 bg-neutral-900/80 hover:bg-neutral-800/80 border border-neutral-800 hover:border-neutral-700 rounded-lg transition-all group"
            >
                <div className="flex items-center gap-1">
                    {stints.length > 0 ? (
                        stints.map(s => {
                            const cfg = compoundConfig[s.compound] || compoundConfig.MEDIUM;
                            return (
                                <div
                                    key={s.stint_number}
                                    className="w-2.5 h-2.5 rounded-full"
                                    style={{ backgroundColor: cfg.color, boxShadow: `0 0 4px ${cfg.color}80` }}
                                />
                            );
                        })
                    ) : (
                        <div
                            className="w-2.5 h-2.5 rounded-full"
                            style={{ backgroundColor: `#${driver.team_colour || '666'}` }}
                        />
                    )}
                </div>
                <span className="text-[11px] font-bold uppercase tracking-wider text-neutral-400 group-hover:text-neutral-200 transition-colors">
                    Tyre Strategy
                </span>
                <ChevronDown
                    className={`w-3.5 h-3.5 ml-auto text-neutral-600 group-hover:text-neutral-400 transition-transform duration-200 ${expanded ? 'rotate-180' : ''}`}
                />
            </button>

            {/* Expandable Content */}
            <AnimatePresence>
                {expanded && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: 'auto', opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        transition={{ duration: 0.25, ease: 'easeInOut' }}
                        className="overflow-hidden"
                    >
                        <div className="pt-3 pb-2">
                            {loading ? (
                                <div className="flex items-center justify-center py-6 gap-2">
                                    <Loader2 className="w-4 h-4 animate-spin text-neutral-500" />
                                    <span className="text-neutral-500 text-xs">Loading stints...</span>
                                </div>
                            ) : error ? (
                                <div className="text-center py-4 text-red-500 text-xs">{error}</div>
                            ) : stints.length === 0 ? (
                                <div className="text-center py-4 text-neutral-500 text-xs">No stint data available</div>
                            ) : (
                                <div className="space-y-1.5">
                                    {/* Stint bar */}
                                    <div className="flex gap-[2px] h-12 rounded-md overflow-hidden">
                                        {stints.map((stint, index) => {
                                            const cfg = compoundConfig[stint.compound] || compoundConfig.MEDIUM;
                                            const widthPct = ((stint.lap_end - stint.lap_start + 1) / actualTotalLaps) * 100;
                                            const laps = stint.lap_end - stint.lap_start + 1;
                                            const textAlpha = cfg.darkText ? 'rgba(0,0,0,0.72)' : 'rgba(255,255,255,0.92)';
                                            const subAlpha = cfg.darkText ? 'rgba(0,0,0,0.45)' : 'rgba(255,255,255,0.55)';
                                            const badgeBg = cfg.darkText ? 'rgba(0,0,0,0.12)' : 'rgba(0,0,0,0.22)';
                                            const badgeBorder = cfg.darkText ? 'rgba(0,0,0,0.18)' : 'rgba(255,255,255,0.22)';

                                            return (
                                                <motion.div
                                                    key={stint.stint_number}
                                                    initial={{ opacity: 0, scaleY: 0.3 }}
                                                    animate={{ opacity: 1, scaleY: 1 }}
                                                    transition={{
                                                        delay: index * 0.06,
                                                        duration: 0.35,
                                                        ease: [0.34, 1.56, 0.64, 1],
                                                    }}
                                                    style={{
                                                        width: `${widthPct}%`,
                                                        minWidth: 28,
                                                        backgroundColor: cfg.color,
                                                        transformOrigin: 'bottom',
                                                    }}
                                                    className="relative flex flex-col items-center justify-center gap-[3px] overflow-hidden cursor-default"
                                                    title={`${cfg.fullLabel} · Laps ${stint.lap_start}–${stint.lap_end}${stint.tyre_age > 0 ? ` · +${stint.tyre_age} laps old` : ''}`}
                                                >
                                                    {/* Depth layers */}
                                                    <div className="absolute inset-x-0 top-0 h-2/5 bg-gradient-to-b from-white/20 to-transparent pointer-events-none" />
                                                    <div className="absolute inset-x-0 bottom-0 h-1/3 bg-gradient-to-t from-black/20 to-transparent pointer-events-none" />

                                                    {/* Tyre badge */}
                                                    <div
                                                        className="relative z-10 w-[18px] h-[18px] rounded-full flex items-center justify-center"
                                                        style={{
                                                            backgroundColor: badgeBg,
                                                            border: `1px solid ${badgeBorder}`,
                                                            boxShadow: 'inset 0 1px 2px rgba(0,0,0,0.15)',
                                                        }}
                                                    >
                                                        <span
                                                            className="text-[10px] font-black leading-none"
                                                            style={{ color: textAlpha }}
                                                        >
                                                            {cfg.label}
                                                        </span>
                                                    </div>

                                                    {/* Lap count */}
                                                    {widthPct > 7 && (
                                                        <span
                                                            className="relative z-10 text-[8px] font-bold leading-none tabular-nums"
                                                            style={{ color: subAlpha }}
                                                        >
                                                            {laps}L
                                                        </span>
                                                    )}
                                                </motion.div>
                                            );
                                        })}
                                    </div>

                                    {/* Lap range labels aligned to segments */}
                                    <div className="flex gap-[2px]">
                                        {stints.map((stint) => {
                                            const widthPct = ((stint.lap_end - stint.lap_start + 1) / actualTotalLaps) * 100;
                                            return (
                                                <div
                                                    key={stint.stint_number}
                                                    style={{ width: `${widthPct}%`, minWidth: 28 }}
                                                    className="overflow-hidden"
                                                >
                                                    <span className="text-[9px] font-mono text-neutral-600 tabular-nums truncate block leading-none">
                                                        {stint.lap_start}–{stint.lap_end}
                                                    </span>
                                                </div>
                                            );
                                        })}
                                    </div>
                                </div>
                            )}
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}
