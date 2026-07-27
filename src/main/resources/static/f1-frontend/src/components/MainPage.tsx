import { useState } from 'react';
import { motion } from 'framer-motion';
import { Calendar, Flag, Trophy, LineChart, ChevronRight } from 'lucide-react';
import SessionSelector from './SessionSelector';
import StandingsPanel from './StandingsPanel';

interface Props {
  onSessionSelect: (sessionKey: string, sessionType: string, meetingName: string, sessionName: string) => void;
}

const YEARS = ['2026', '2025', '2024', '2023'];

export default function MainPage({ onSessionSelect }: Props) {
  const [year, setYear] = useState<string>('2026');

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0 }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 md:p-8 relative">
      <motion.div
        className="max-w-5xl w-full"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        {/* Header */}
        <motion.div variants={itemVariants} className="mb-10 text-center">
          <div className="inline-flex items-center gap-2 bg-red-600/10 border border-red-600/20 rounded-full px-4 py-2 mb-6">
            <Flag className="w-4 h-4 text-red-500" />
            <span className="text-red-500 font-bold tracking-wider text-xs uppercase">
              F1 {year} Season
            </span>
          </div>
          <h1 className="text-5xl md:text-6xl font-black tracking-tighter bg-gradient-to-r from-white via-white to-neutral-400 bg-clip-text text-transparent">
            F1 DATA
          </h1>
          <h1 className="text-5xl md:text-6xl font-black tracking-tighter text-red-600 -mt-2">
            VISUALIZER
          </h1>
        </motion.div>

        {/* Year Select */}
        <motion.div variants={itemVariants} className="max-w-xs mx-auto mb-10">
          <label className="flex items-center justify-center gap-2 text-xs font-bold text-neutral-400 mb-3 uppercase tracking-wider">
            <Calendar className="w-4 h-4" />
            Season Year
          </label>
          <div className="relative">
            <select
              value={year}
              onChange={(e) => setYear(e.target.value)}
              className="w-full bg-neutral-900/50 backdrop-blur-sm border border-neutral-800 text-white p-4 rounded-xl focus:outline-none focus:border-red-600 focus:ring-2 focus:ring-red-600/20 transition-all cursor-pointer appearance-none hover:border-neutral-700 text-center"
            >
              {YEARS.map((y) => (
                <option key={y} value={y}>{y}</option>
              ))}
            </select>
            <ChevronRight className="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-500 rotate-90 pointer-events-none" />
          </div>
        </motion.div>

        {/* Two sections: Standings + Session Analysis */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
          {/* Standings */}
          <motion.section
            variants={itemVariants}
            className="glass rounded-2xl p-6"
          >
            <div className="flex items-center gap-3 mb-6">
              <div className="p-2 bg-red-600/10 border border-red-600/20 rounded-lg">
                <Trophy className="w-5 h-5 text-red-500" />
              </div>
              <div>
                <h2 className="font-black tracking-tight text-lg leading-tight">Championship Standings</h2>
                <p className="text-neutral-500 text-xs">Drivers & constructors points for {year}</p>
              </div>
            </div>
            <StandingsPanel year={parseInt(year)} />
          </motion.section>

          {/* Session Analysis */}
          <motion.section
            variants={itemVariants}
            className="glass rounded-2xl p-6"
          >
            <div className="flex items-center gap-3 mb-6">
              <div className="p-2 bg-red-600/10 border border-red-600/20 rounded-lg">
                <LineChart className="w-5 h-5 text-red-500" />
              </div>
              <div>
                <h2 className="font-black tracking-tight text-lg leading-tight">Session Analysis</h2>
                <p className="text-neutral-500 text-xs">Pick a Grand Prix and session to view detailed results</p>
              </div>
            </div>
            <SessionSelector year={parseInt(year)} onSessionSelect={onSessionSelect} />
          </motion.section>
        </div>

        {/* Footer */}
        <motion.div variants={itemVariants} className="mt-10 text-center text-neutral-600 text-xs">
          <p>Data powered by OpenF1 API</p>
        </motion.div>
      </motion.div>
    </div>
  );
}
