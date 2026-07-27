import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Trophy, Users, Loader2 } from 'lucide-react';
import { getDriverStandings, getTeamStandings } from '../services/api';
import type { DriverStanding, TeamStanding } from '../types';

interface Props {
  year: number;
}

type Tab = 'drivers' | 'teams';

export default function StandingsPanel({ year }: Props) {
  const [tab, setTab] = useState<Tab>('drivers');
  const [driverStandings, setDriverStandings] = useState<DriverStanding[]>([]);
  const [teamStandings, setTeamStandings] = useState<TeamStanding[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    const fetchStandings = async () => {
      setLoading(true);
      setError(false);
      try {
        const [drivers, teams] = await Promise.all([
          getDriverStandings(year, controller.signal),
          getTeamStandings(year, controller.signal),
        ]);
        setDriverStandings([...drivers].sort((a, b) => b.points - a.points));
        setTeamStandings([...teams].sort((a, b) => b.points - a.points));
      } catch (err) {
        if (!controller.signal.aborted) {
          console.error('Failed to fetch standings', err);
          setDriverStandings([]);
          setTeamStandings([]);
          setError(true);
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };
    fetchStandings();
    return () => controller.abort();
  }, [year]);

  const maxDriverPoints = driverStandings[0]?.points || 1;
  const maxTeamPoints = teamStandings[0]?.points || 1;

  const positionStyle = (pos: number) => {
    if (pos === 1) return 'text-yellow-400';
    if (pos === 2) return 'text-neutral-300';
    if (pos === 3) return 'text-amber-600';
    return 'text-neutral-500';
  };

  return (
    <div className="flex flex-col h-full">
      {/* Tab toggle */}
      <div className="flex gap-2 mb-4 bg-neutral-900/50 border border-neutral-800 rounded-xl p-1">
        <button
          onClick={() => setTab('drivers')}
          className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-lg text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
            tab === 'drivers'
              ? 'bg-red-600 text-white shadow-lg shadow-red-900/30'
              : 'text-neutral-400 hover:text-white'
          }`}
        >
          <Users className="w-4 h-4" />
          Drivers
        </button>
        <button
          onClick={() => setTab('teams')}
          className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-lg text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
            tab === 'teams'
              ? 'bg-red-600 text-white shadow-lg shadow-red-900/30'
              : 'text-neutral-400 hover:text-white'
          }`}
        >
          <Trophy className="w-4 h-4" />
          Constructors
        </button>
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex-1 flex items-center justify-center py-16 text-neutral-500">
          <Loader2 className="w-6 h-6 animate-spin" />
        </div>
      ) : error ? (
        <div className="flex-1 flex items-center justify-center py-16 text-neutral-500 text-sm">
          No standings available for {year}
        </div>
      ) : (
        <AnimatePresence mode="wait">
          <motion.div
            key={tab}
            initial={{ opacity: 0, x: tab === 'drivers' ? -10 : 10 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="flex-1 overflow-y-auto max-h-[26rem] pr-1"
          >
            {tab === 'drivers' ? (
              <ul className="flex flex-col gap-1">
                {driverStandings.map((standing, i) => (
                  <li
                    key={`${standing.driver_number}-${standing.full_name}`}
                    className="relative flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-neutral-800/50 transition-colors overflow-hidden"
                  >
                    <div
                      className="absolute inset-y-0 left-0 bg-red-600/5"
                      style={{ width: `${(standing.points / maxDriverPoints) * 100}%` }}
                    />
                    <span className={`relative w-6 text-right font-black tabular-nums ${positionStyle(i + 1)}`}>
                      {i + 1}
                    </span>
                    <span className="relative w-8 text-xs font-bold text-neutral-500 tabular-nums">
                      #{standing.driver_number}
                    </span>
                    <span className="relative flex-1 font-semibold text-sm truncate">
                      {standing.full_name ?? `Driver #${standing.driver_number}`}
                    </span>
                    <span className="relative font-black tabular-nums text-sm">
                      {standing.points}
                      <span className="ml-1 text-[10px] font-bold text-neutral-500 uppercase">pts</span>
                    </span>
                  </li>
                ))}
              </ul>
            ) : (
              <ul className="flex flex-col gap-1">
                {teamStandings.map((standing, i) => (
                  <li
                    key={standing.team_name}
                    className="relative flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-neutral-800/50 transition-colors overflow-hidden"
                  >
                    <div
                      className="absolute inset-y-0 left-0 bg-red-600/5"
                      style={{ width: `${(standing.points / maxTeamPoints) * 100}%` }}
                    />
                    <span className={`relative w-6 text-right font-black tabular-nums ${positionStyle(i + 1)}`}>
                      {i + 1}
                    </span>
                    <div
                      className="relative w-1 h-6 rounded-full shrink-0"
                      style={{ backgroundColor: `#${standing.team_colour || '555555'}` }}
                    />
                    <span className="relative flex-1 font-semibold text-sm truncate">
                      {standing.team_name}
                    </span>
                    <span className="relative font-black tabular-nums text-sm">
                      {standing.points}
                      <span className="ml-1 text-[10px] font-bold text-neutral-500 uppercase">pts</span>
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </motion.div>
        </AnimatePresence>
      )}
    </div>
  );
}
