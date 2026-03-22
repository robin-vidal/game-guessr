import { useRef } from 'react';
import { useParams } from 'react-router';
import { useGame } from '@/contexts/game/GameContext';
import { GamePhase } from '@/types';
import GameHUD from './components/GameHUD';
import PlaceholderPanel from './components/PlaceholderPanel';

/**
 * GamePage wires together:
 *  - The noclip iframe (3D exploration)
 *  - The phase-specific guess panels
 *  - The HUD (timer, round counter, current phase)
 *
 * Each phase panel is its own component so they can be developed and
 * tested in isolation.
 */
export function GamePage() {
  const { roomId } = useParams<{ roomId: string }>();
  console.log({ roomId });
  const { round, timeRemaining, currentPhase } = useGame();
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const iframeUrl = import.meta.env.NOCLIP_FRONTEND_URL || 'http://localhost:8000';

  return (
    <div className="relative h-screen w-screen overflow-hidden bg-background">
      <iframe
        ref={iframeRef}
        src={iframeUrl}
        className="absolute inset-0 h-full w-full border-0"
        allow="fullscreen"
        title="Noclip 3D viewer"
      />

      <GameHUD
        roundNumber={round?.roundNumber ?? 0}
        totalRounds={round?.totalRounds ?? 0}
        phase={currentPhase}
        timeRemaining={timeRemaining}
      />

      <div className="absolute bottom-6 left-1/2 -translate-x-1/2 w-full max-w-md px-4">
        {currentPhase === GamePhase.GUESSING_GAME && (
          <PlaceholderPanel label="Phase 1" description="Which game is this from?" />
        )}
        {currentPhase === GamePhase.GUESSING_LEVEL && (
          <PlaceholderPanel label="Phase 3" description="Drop a pin on the map." />
        )}
        {currentPhase === GamePhase.GUESSING_SPOT && (
          <PlaceholderPanel label="Phase 3" description="Drop a pin on the map." />
        )}
        {currentPhase === GamePhase.ROUND_RESULTS && (
          <PlaceholderPanel label="Round over" description="Calculating scores…" />
        )}
        {currentPhase === null && <PlaceholderPanel label="Error" description="Game not found" />}
      </div>
    </div>
  );
}
