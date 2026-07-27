import { useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import MainPage from './components/MainPage';
import ResultDashboard from './components/ResultDashboard';
import CarDataDashboard from './components/CarDataDashboard';

interface SelectedSession {
  sessionKey: string;
  sessionType: string;
  meetingName: string;
  sessionName: string;
}

type View = 'selector' | 'results' | 'cardata';

export default function App() {
  const [selectedSession, setSelectedSession] = useState<SelectedSession | null>(null);
  const [view, setView] = useState<View>('selector');

  const handleSessionSelect = (key: string, type: string, meetingName: string, sessionName: string) => {
    setSelectedSession({ sessionKey: key, sessionType: type, meetingName, sessionName });
    setView('results');
  };

  const handleBack = () => {
    setSelectedSession(null);
    setView('selector');
  };

  const handleOpenCarData = () => setView('cardata');
  const handleBackToResults = () => setView('results');

  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-950 via-neutral-900 to-neutral-950 text-white font-sans selection:bg-red-600 selection:text-white overflow-hidden">
      {/* Background decorative elements */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden">
        <div className="absolute -top-40 -right-40 w-96 h-96 bg-red-600/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-red-600/5 rounded-full blur-3xl" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-gradient-radial from-red-900/5 to-transparent rounded-full" />
      </div>

      <AnimatePresence mode="wait">
        {view === 'selector' && (
          <motion.div
            key="selector"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4, ease: 'easeOut' }}
          >
            <MainPage onSessionSelect={handleSessionSelect} />
          </motion.div>
        )}

        {view === 'results' && selectedSession && (
          <motion.div
            key="dashboard"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4, ease: 'easeOut' }}
          >
            <ResultDashboard
              sessionKey={selectedSession.sessionKey}
              sessionType={selectedSession.sessionType}
              meetingName={selectedSession.meetingName}
              sessionName={selectedSession.sessionName}
              onBack={handleBack}
              onOpenCarData={handleOpenCarData}
            />
          </motion.div>
        )}

        {view === 'cardata' && selectedSession && (
          <motion.div
            key="cardata"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4, ease: 'easeOut' }}
          >
            <CarDataDashboard
              sessionKey={selectedSession.sessionKey}
              meetingName={selectedSession.meetingName}
              sessionName={selectedSession.sessionName}
              onBack={handleBackToResults}
            />
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}