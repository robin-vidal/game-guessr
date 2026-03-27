import { useRef } from 'react';
import { useNavigate, useParams } from 'react-router';
import GameHUD from './components/GameHUD';
import PlaceholderPanel from './components/PlaceholderPanel';
import { useQuery } from '@tanstack/react-query';
import { defaultConfig } from '@/client/config';
import { getRoomOptions } from '@/client/lobby-service/@tanstack/react-query.gen';
import LoadingScreen from '@/components/ui/LoadingScreen';
import { getCurrentRoundOptions } from '@/client/game-service/@tanstack/react-query.gen';
import GuessGameInput from './components/guess-inputs/GuessGameInput';
import GuessLevelInput from './components/guess-inputs/GuessLevelInput';
import { config } from '@/config';

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
  const navigate = useNavigate();

  const { data: room, isPending: isGamePending } = useQuery({
    ...getRoomOptions({ path: { code: roomId! }, ...defaultConfig }),
    refetchInterval: (query) => {
      if (query.state.data?.status !== 'IN_PROGRESS') return false;
      return 60 * 1000; // poll every minute
    },
  });

  const { data: round, isPending: isRoundPending } = useQuery({
    ...getCurrentRoundOptions({ path: { code: roomId! }, ...defaultConfig }),
    refetchInterval: (query) => {
      // Stop polling once the round is over
      if (query.state.data?.currentPhase === null || query.state.data?.finished) return false;
      return 2000; // poll every 2s
    },
  });

  const iframeRef = useRef<HTMLIFrameElement>(null);

  if (isGamePending || isRoundPending) return <LoadingScreen />;

  if (!roomId || !room || !round) {
    navigate('/');
    return null;
  }

  const iframeBaseUrl = config.noclipFrontendUrl;

  const iframeHash = round.noclipHash ?? '';
  const iframeUrl = `${iframeBaseUrl}/#${iframeHash}`;
  const currentPhase = round.currentPhase;

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
        roundNumber={round.roundNumber ?? 0}
        totalRounds={room?.settings?.roundCount ?? 0}
        phase={round.currentPhase}
        startedAt={round.startedAt ?? 0}
        roundTimeInSeconds={room.settings?.timeLimitSeconds ?? 300}
      />

      <div className="absolute bottom-6 left-1/2 -translate-x-1/2 w-full max-w-md px-4">
        {currentPhase === 'SPOT' && (
          <PlaceholderPanel label="Phase 2" description="Place le point sur la carte" />
        )}
        {currentPhase === 'GAME' && <GuessGameInput roomId={roomId} />}
        {currentPhase === 'LEVEL' && <GuessLevelInput roomId={roomId} />}

        {!currentPhase && <PlaceholderPanel label="Erreur" description="Partie non trouvée" />}
      </div>
    </div>
  );
}
