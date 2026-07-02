import { useEffect, useState, useCallback } from 'react';
import { motion } from 'framer-motion';
import ComparisonPanel from './ComparisonPanel';
import type { ComparisonEntry } from './ComparisonPanel';

/**
 * Standalone page for the Comparison Panel.
 * Receives data from the parent page via BroadcastChannel.
 */
export default function ComparisonPage() {
    const [entries, setEntries] = useState<ComparisonEntry[]>([]);
    const [sessionKey, setSessionKey] = useState<string>('');
    const [ready, setReady] = useState(false);

    useEffect(() => {
        const channel = new BroadcastChannel('f1-comparison');

        channel.onmessage = (event) => {
            const { type, payload } = event.data;
            if (type === 'update') {
                setSessionKey(payload.sessionKey);
                setEntries(payload.entries);
                setReady(true);
            } else if (type === 'clear') {
                setEntries([]);
            }
        };

        // Request initial data from parent
        channel.postMessage({ type: 'request-data' });

        return () => channel.close();
    }, []);

    const handleRemoveEntry = useCallback((selectionId: string, lapNumber: number) => {
        const channel = new BroadcastChannel('f1-comparison');
        channel.postMessage({ type: 'remove-entry', payload: { selectionId, lapNumber } });
        channel.close();
    }, []);

    const handleClearAll = useCallback(() => {
        const channel = new BroadcastChannel('f1-comparison');
        channel.postMessage({ type: 'clear-all' });
        channel.close();
    }, []);

    const handleColorChange = useCallback((selectionId: string, newColor: string) => {
        const channel = new BroadcastChannel('f1-comparison');
        channel.postMessage({ type: 'color-change', payload: { selectionId, newColor } });
        channel.close();
    }, []);

    if (!ready || !sessionKey) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-neutral-950 via-neutral-900 to-neutral-950 text-white flex items-center justify-center">
                <div className="fixed inset-0 pointer-events-none overflow-hidden">
                    <div className="absolute -top-40 -right-40 w-96 h-96 bg-purple-600/10 rounded-full blur-3xl" />
                    <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-purple-600/5 rounded-full blur-3xl" />
                </div>
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="flex flex-col items-center gap-4 relative z-10"
                >
                    <div className="w-8 h-8 border-3 border-purple-500 border-t-transparent rounded-full animate-spin" />
                    <span className="text-neutral-400 text-sm font-bold uppercase tracking-widest">Waiting for comparison data…</span>
                    <span className="text-neutral-600 text-xs">Select laps from the telemetry dashboard</span>
                </motion.div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-neutral-950 via-neutral-900 to-neutral-950 text-white font-sans selection:bg-purple-600 selection:text-white">
            <div className="fixed inset-0 pointer-events-none overflow-hidden">
                <div className="absolute -top-40 -right-40 w-96 h-96 bg-purple-600/10 rounded-full blur-3xl" />
                <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-purple-600/5 rounded-full blur-3xl" />
            </div>
            <div className="relative z-10 p-4 md:p-8 max-w-[1600px] mx-auto">
                {entries.length > 0 ? (
                    <ComparisonPanel
                        sessionKey={sessionKey}
                        entries={entries}
                        onRemoveEntry={handleRemoveEntry}
                        onClearAll={handleClearAll}
                        onColorChange={handleColorChange}
                    />
                ) : (
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        className="flex flex-col items-center justify-center min-h-[60vh] gap-4"
                    >
                        <span className="text-neutral-500 text-sm font-bold uppercase tracking-widest">No laps selected</span>
                        <span className="text-neutral-600 text-xs">Click laps on the telemetry dashboard to compare</span>
                    </motion.div>
                )}
            </div>
        </div>
    );
}
