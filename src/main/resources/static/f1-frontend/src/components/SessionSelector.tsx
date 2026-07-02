import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Calendar, MapPin, Flag, ChevronRight, Loader2 } from 'lucide-react';
import { getMeetings, getSessions } from '../services/api';
import type { Meeting, Session } from '../types';

interface Props {
  onSessionSelect: (sessionKey: string, sessionType: string, meetingName: string, sessionName: string) => void;
}

export default function SessionSelector({ onSessionSelect }: Props) {
  // --- State ---
  const [year, setYear] = useState<string>('2026');
  const [meetings, setMeetings] = useState<Meeting[]>([]);
  const [selectedMeetingKey, setSelectedMeetingKey] = useState<string>('');

  const [sessions, setSessions] = useState<Session[]>([]);
  const [selectedSessionKey, setSelectedSessionKey] = useState<string>('');

  const [loading, setLoading] = useState(false);

  const selectedMeeting = meetings.find(m => m.meeting_key.toString() === selectedMeetingKey);
  const selectedSession = sessions.find(s => s.session_key.toString() === selectedSessionKey);

  // --- 1. Fetch Meetings when Year changes ---
  useEffect(() => {
    const fetchMeetings = async () => {
      setLoading(true);
      try {
        const res = await getMeetings(parseInt(year));
        setMeetings(res);
        setSelectedMeetingKey('');
        setSessions([]);
      } catch (err) {
        console.error("Failed to fetch meetings", err);
      } finally {
        setLoading(false);
      }
    };
    fetchMeetings();
  }, [year]);

  // --- 2. Fetch Sessions when Meeting changes ---
  useEffect(() => {
    if (!selectedMeetingKey) return;

    const fetchSessions = async () => {
      setLoading(true);
      try {
        const res = await getSessions(parseInt(selectedMeetingKey));
        setSessions(res);
        setSelectedSessionKey('');
      } catch (err) {
        console.error("Failed to fetch sessions", err);
      } finally {
        setLoading(false);
      }
    };
    fetchSessions();
  }, [selectedMeetingKey]);

  // --- Handle Form Submission ---
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedSessionKey && selectedMeeting && selectedSession) {
      onSessionSelect(selectedSessionKey, selectedSession.session_type, selectedMeeting.meeting_name, selectedSession.session_name);
    }
  };

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
    <div className="min-h-screen flex items-center justify-center p-4 relative">
      <motion.div
        className="max-w-lg w-full"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        {/* Header */}
        <motion.div variants={itemVariants} className="mb-12 text-center">
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
          <p className="text-neutral-500 mt-4 text-sm">
            Choose a Grand Prix and session to view detailed results
          </p>
        </motion.div>

        {/* Form */}
        <motion.form variants={itemVariants} onSubmit={handleSubmit} className="flex flex-col gap-6">
          {/* Year Select */}
          <motion.div
            variants={itemVariants}
            className="group"
          >
            <label className="flex items-center gap-2 text-xs font-bold text-neutral-400 mb-3 uppercase tracking-wider">
              <Calendar className="w-4 h-4" />
              Season Year
            </label>
            <div className="relative">
              <select
                value={year}
                onChange={(e) => setYear(e.target.value)}
                disabled={loading}
                className="w-full bg-neutral-900/50 backdrop-blur-sm border border-neutral-800 text-white p-4 rounded-xl focus:outline-none focus:border-red-600 focus:ring-2 focus:ring-red-600/20 transition-all disabled:opacity-50 cursor-pointer appearance-none hover:border-neutral-700"
              >
                <option value="2026">2026</option>
                <option value="2025">2025</option>
                <option value="2024">2024</option>
                <option value="2023">2023</option>
              </select>
              <ChevronRight className="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-500 rotate-90 pointer-events-none" />
            </div>
          </motion.div>

          {/* Meeting Select */}
          <motion.div variants={itemVariants} className="group">
            <label className="flex items-center gap-2 text-xs font-bold text-neutral-400 mb-3 uppercase tracking-wider">
              <MapPin className="w-4 h-4" />
              Grand Prix
            </label>
            <div className="relative">
              <select
                value={selectedMeetingKey}
                onChange={(e) => setSelectedMeetingKey(e.target.value)}
                disabled={meetings.length === 0 || loading}
                className="w-full bg-neutral-900/50 backdrop-blur-sm border border-neutral-800 text-white p-4 rounded-xl focus:outline-none focus:border-red-600 focus:ring-2 focus:ring-red-600/20 transition-all disabled:opacity-50 cursor-pointer appearance-none hover:border-neutral-700"
              >
                <option value="" disabled>Select Grand Prix...</option>
                {meetings.map((m) => (
                  <option key={m.meeting_key} value={m.meeting_key}>
                    {m.meeting_name} • {m.location}
                  </option>
                ))}
              </select>
              <ChevronRight className="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-500 rotate-90 pointer-events-none" />
            </div>
            {selectedMeeting && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                className="mt-3 flex items-center gap-3 text-sm"
              >
                <span className="text-neutral-500">📍 {selectedMeeting.location}</span>
                <span className="text-neutral-700">•</span>
                <span className="text-neutral-500">{selectedMeeting.country_name}</span>
              </motion.div>
            )}
          </motion.div>

          {/* Session Select */}
          <motion.div variants={itemVariants} className="group">
            <label className="flex items-center gap-2 text-xs font-bold text-neutral-400 mb-3 uppercase tracking-wider">
              <Flag className="w-4 h-4" />
              Session
            </label>
            <div className="relative">
              <select
                value={selectedSessionKey}
                onChange={(e) => setSelectedSessionKey(e.target.value)}
                disabled={!selectedMeetingKey || sessions.length === 0 || loading}
                className="w-full bg-neutral-900/50 backdrop-blur-sm border border-neutral-800 text-white p-4 rounded-xl focus:outline-none focus:border-red-600 focus:ring-2 focus:ring-red-600/20 transition-all disabled:opacity-50 cursor-pointer appearance-none hover:border-neutral-700"
              >
                <option value="" disabled>Select Session...</option>
                {sessions.map((s) => (
                  <option key={s.session_key} value={s.session_key}>
                    {s.session_name}
                  </option>
                ))}
              </select>
              <ChevronRight className="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 text-neutral-500 rotate-90 pointer-events-none" />
            </div>
          </motion.div>

          {/* Submit Button */}
          <motion.button
            variants={itemVariants}
            type="submit"
            disabled={!selectedSessionKey || loading}
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            className="mt-6 relative bg-gradient-to-r from-red-600 to-red-700 hover:from-red-500 hover:to-red-600 text-white font-bold py-4 rounded-xl uppercase tracking-wider transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-red-900/30 hover:shadow-red-600/40 overflow-hidden group"
          >
            <span className="relative z-10 flex items-center justify-center gap-2">
              {loading ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Loading...
                </>
              ) : (
                <>
                  View Results
                  <ChevronRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
                </>
              )}
            </span>
            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent -translate-x-full group-hover:translate-x-full transition-transform duration-700" />
          </motion.button>
        </motion.form>

        {/* Footer */}
        <motion.div variants={itemVariants} className="mt-12 text-center text-neutral-600 text-xs">
          <p>Data powered by OpenF1 API</p>
        </motion.div>
      </motion.div>
    </div>
  );
}

