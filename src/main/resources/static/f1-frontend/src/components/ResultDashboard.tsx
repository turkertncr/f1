import {useEffect, useState} from 'react';
import {motion} from 'framer-motion';
import {ArrowLeft, Trophy, Clock, Flag, Timer, Award, Zap, Activity} from 'lucide-react';
import {getResults} from '../services/api';
import type {Result} from '../types';

interface Props {
    sessionKey: string;
    sessionType: string;
    meetingName: string;
    sessionName: string;
    onBack: () => void;
    onOpenCarData: () => void;
}

const isQualifying = (sessionType: string) => {
    return sessionType.toLowerCase().includes('qualifying');
};

const formatGap = (gap: string[] | null, position: number, dnf: boolean, dns: boolean, dsq: boolean) => {
    if (dnf) return "DNF";
    if (dsq) return "DSQ";
    if (dns) return "DNS";
    if (position === 1) return "WINNER";
    if (!gap || gap.length === 0) return "-";

    let ret = gap[0];
    if (ret.at(0) != "+") {
        ret = "+" + ret;
    }
    return ret;
};

const formatDurationToTime = (duration: number | undefined): string => {
    if (duration === undefined || duration === null || duration <= 0) return "-";

    const totalSeconds = duration;
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    // Format as m:ss.sss
    return `${minutes}:${seconds.toFixed(3).padStart(6, '0')}`;
};


export default function ResultDashboard({ sessionKey, sessionType, meetingName, sessionName, onBack, onOpenCarData }: Props) {
    const [results, setResults] = useState<Result[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const controller = new AbortController();

        const fetchData = async () => {
            setLoading(true);
            try {
                const data = await getResults(parseInt(sessionKey), controller.signal);
                if (!controller.signal.aborted) setResults(data);
            } catch (err) {
                console.error("Error fetching race data:", err);
            } finally {
                if (!controller.signal.aborted) setLoading(false);
            }
        };

        fetchData();

        return () => {
            controller.abort();
        };
    }, [sessionKey]);

    if (loading) return (
        <div className="min-h-screen flex flex-col items-center justify-center gap-6">
            <motion.div
                initial={{ scale: 0.8, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                className="relative"
            >
                <div className="w-20 h-20 border-4 border-neutral-800 rounded-full"></div>
                <div className="absolute inset-0 w-20 h-20 border-4 border-red-600 border-t-transparent rounded-full animate-spin"></div>
                <div className="absolute inset-2 w-16 h-16 border-4 border-red-400/30 border-t-transparent rounded-full animate-spin" style={{ animationDirection: 'reverse', animationDuration: '1.5s' }}></div>
            </motion.div>
            <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.2 }}
                className="text-center"
            >
                <div className="text-white font-mono tracking-widest text-sm mb-2">
                    LOADING DATA
                </div>
                <div className="text-neutral-500 text-xs">
                    Fetching {sessionName} results...
                </div>
            </motion.div>
        </div>
    );

    const qualifying = isQualifying(sessionType);

    // Get podium drivers for highlight
    const podiumDrivers = results.slice(0, 3);
    const winner = podiumDrivers[0];



    return (
        <div className="p-4 md:p-8 max-w-6xl mx-auto relative">

            {/* Header */}
            <motion.div
                initial={{ opacity: 0, y: -20 }}
                animate={{ opacity: 1, y: 0 }}
                className="mb-8"
            >
                <div className="flex items-center gap-3 mb-4">
                    <button
                        onClick={onBack}
                        className="text-sm text-neutral-500 hover:text-white flex items-center gap-2 transition-colors uppercase tracking-wider font-bold group"
                    >
                        <ArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
                        Back to Selection
                    </button>
                    <div className="flex-1" />
                    <button
                        onClick={onOpenCarData}
                        className="flex items-center gap-2 bg-cyan-600/10 hover:bg-cyan-600/20 border border-cyan-600/30 hover:border-cyan-500/50 text-cyan-400 text-xs font-bold uppercase tracking-wider px-3 py-1.5 rounded-lg transition-all"
                    >
                        <Activity className="w-3.5 h-3.5" />
                        Telemetry
                    </button>
                </div>

                <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
                    <div>
                        <div className="inline-flex items-center gap-2 bg-red-600/10 border border-red-600/20 rounded-full px-3 py-1 mb-3">
                            <Flag className="w-3 h-3 text-red-500" />
                            <span className="text-red-500 font-bold tracking-wider text-xs uppercase">
                                {sessionType}
                            </span>
                        </div>
                        <h1 className="text-3xl md:text-4xl font-black tracking-tight uppercase">
                            {meetingName}
                        </h1>
                        <p className="text-neutral-500 mt-1">{sessionName} Results</p>
                    </div>

                    {/* Stats Cards */}
                    <div className="flex gap-3">
                        {!qualifying && winner && (
                            <div className="bg-gradient-to-br from-yellow-600/20 to-yellow-700/10 backdrop-blur-sm border border-yellow-600/30 rounded-xl px-4 py-3">
                                <div className="flex items-center gap-2 text-yellow-500 text-xs mb-1">
                                    <Trophy className="w-3 h-3" />
                                    Winner
                                </div>
                                <div className="text-xl font-bold text-white">{winner.driver.name_acronym}</div>
                            </div>
                        )}
                        {qualifying && winner && (
                            <div className="bg-gradient-to-br from-purple-600/20 to-purple-700/10 backdrop-blur-sm border border-purple-600/30 rounded-xl px-4 py-3">
                                <div className="flex items-center gap-2 text-purple-500 text-xs mb-1">
                                    <Zap className="w-3 h-3" />
                                    Pole
                                </div>
                                <div className="text-xl font-bold text-white">{winner.driver.name_acronym}</div>
                            </div>
                        )}
                    </div>
                </div>
            </motion.div>

            {/* Results Table */}
            <div className="flex flex-col gap-2">

                {/* Columns Header */}
                <div className="flex px-4 py-3 text-[10px] md:text-xs font-bold text-neutral-600 uppercase tracking-widest bg-black rounded-lg border-b border-neutral-800">
                    <div className="w-10 text-center"></div>
                    <div className="w-10 text-center"></div>
                    <div className="w-14"></div>
                    <div className="flex-1"></div>
                    <div className="w-40 hidden md:block"></div>
                    {qualifying ? (
                        <>
                            <div className="w-32 md:w-36 text-right flex items-center justify-end gap-1">
                                <Timer className="w-3 h-3" />Best Time
                            </div>
                        </>
                    ) : (
                        <>
                            <div className="w-16 md:w-20 text-center hidden sm:block">Laps</div>
                            <div className="w-24 md:w-32 text-right flex items-center justify-end gap-1">
                                <Clock className="w-3 h-3" />Time
                            </div>
                        </>
                    )}
                </div>

                {results.map((entry, index) => {
                    const {driver, gap_to_leader, dnf, dns, dsq} = entry;
                    const position = index + 1; // Use sorted index as position

                    const borderColor = driver.team_colour ? `#${driver.team_colour}` : '#333';

                    // Best lap time from the backend (single value for qualifying)
                    const bestTime = entry.duration?.[0];

                    // Position styling
                    const isTop3 = position <= 3;

                    return (
                        <motion.div
                            key={driver.driver_number}
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.25, delay: index * 0.03 }}
                            className="flex flex-col"
                        >
                            <div className="group relative flex items-center bg-black hover:bg-neutral-900 transition-colors duration-200 border-b border-neutral-800/50">
                                {/* Finishing Position */}
                                <div className="w-10 py-3 flex items-center justify-center font-bold text-white text-sm">
                                    {position}
                                </div>

                                {/* Driver Number */}
                                <div className="w-10 py-3 flex items-center justify-center font-bold text-white text-sm">
                                    {driver.driver_number}
                                </div>

                                {/* Driver Image */}
                                <div className="w-14 py-2 flex items-center justify-center">
                                    <div className="w-8 h-8 rounded-full bg-neutral-800 overflow-hidden shrink-0">
                                        <img
                                            src={driver.headshot_url}
                                            alt={driver.name_acronym}
                                            className="w-full h-full object-cover transform translate-y-1 scale-125"
                                            onError={(e) => (e.target as HTMLImageElement).style.display = 'none'}
                                        />
                                    </div>
                                </div>

                                {/* Driver Name */}
                                <div className="flex-1 py-3 flex items-center gap-2">
                                    <div className="font-bold text-white text-sm truncate">
                                        {driver.full_name}
                                    </div>
                                    {isTop3 && !qualifying && (
                                        <Award className={`w-3.5 h-3.5 shrink-0 ${position === 1 ? 'text-yellow-500' : position === 2 ? 'text-gray-400' : 'text-amber-600'}`} />
                                    )}
                                </div>

                                {/* Team Info */}
                                <div className="w-40 hidden md:flex items-center gap-2 py-3">
                                    <div 
                                        className="w-4 h-4 rounded-full flex items-center justify-center shrink-0"
                                        style={{ backgroundColor: borderColor }}
                                    >
                                        <div className="w-2 h-2 rounded-full bg-white/20"></div>
                                    </div>
                                    <span className="text-sm text-white font-medium truncate">
                                        {driver.team_name}
                                    </span>
                                </div>

                                {/* Times and Laps */}
                                {qualifying ? (
                                    <>
                                        <div className={`w-32 md:w-36 pr-4 text-right font-mono font-medium text-xs md:text-sm tracking-tight py-3 ${position === 1 ? 'text-purple-400' : 'text-white/80'}`}>
                                            {formatDurationToTime(bestTime)}
                                        </div>
                                    </>
                                ) : (
                                    <>
                                        <div className="w-16 md:w-20 text-center text-white text-sm font-medium py-3 hidden sm:block">
                                            {entry.laps || '-'}
                                        </div>
                                        <div className={`w-24 md:w-32 pr-4 text-right font-mono font-bold text-sm tracking-tight py-3 ${
                                            position === 1 ? 'text-white' : 
                                            dnf || dsq || dns ? 'text-red-500' : 
                                            'text-white'
                                        }`}>
                                            {formatGap(gap_to_leader, position, dnf, dns, dsq)}
                                        </div>
                                    </>
                                )}
                            </div>
                        </motion.div>
                    );
                })}
            </div>

            {/* Footer */}
            <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.5 }}
                className="mt-8 text-center text-neutral-600 text-xs"
            >
                <p>Data powered by OpenF1 API</p>
            </motion.div>
        </div>
    );
}
