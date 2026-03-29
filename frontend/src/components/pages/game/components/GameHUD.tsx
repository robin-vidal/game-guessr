import { useEffect, useState } from 'react';
import { GetCurrentRoundResponse } from '@/client/game-service';

interface HUDProps {
  roundNumber: number;
  totalRounds: number;
  phase: GetCurrentRoundResponse['currentPhase'] | null;
  startedAt: number;
  roundTimeInSeconds: number;
}

export default function GameHUD({
  roundNumber,
  totalRounds,
  phase,
  roundTimeInSeconds,
  startedAt,
}: Readonly<HUDProps>) {
  const phaseLabel: Record<string, string> = {
    GAME: 'Quel jeu est-ce?',
    LEVEL: 'Quel niveau?',
    SPOT: 'Où précisement?',
  };

  function useCountdown(startedAt: number, durationInSeconds: number): number {
    const getRemaining = () => {
      const elapsed = Math.floor((Date.now() - startedAt) / 1000);
      return Math.max(0, durationInSeconds - elapsed);
    };

    const [timeRemaining, setTimeRemaining] = useState<number>(getRemaining);

    useEffect(() => {
      // Sync immediately in case the component mounted mid-round
      setTimeRemaining(getRemaining());

      const interval = setInterval(() => {
        const remaining = getRemaining();
        setTimeRemaining(remaining);
        if (remaining <= 0) clearInterval(interval);
      }, 1000);

      return () => clearInterval(interval);
    }, [startedAt, durationInSeconds]);

    return timeRemaining;
  }

  const timeRemaining = useCountdown(startedAt, roundTimeInSeconds);
  const isUrgent = timeRemaining <= 10;

  return (
    <div className="absolute top-4 left-1/2 -translate-x-1/2 flex items-center gap-4 rounded-full bg-card border border-border backdrop-blur-sm px-5 py-2 text-foreground">
      {/* Round badge */}
      <span className="rounded-full bg-primary px-3 py-0.5 text-xs font-semibold text-primary-foreground">
        Round {roundNumber}/{totalRounds}
      </span>

      <span className="h-3 w-px bg-border" />

      {/* Phase label */}
      <span className="text-sm font-medium text-foreground">{phase ? phaseLabel[phase] : '—'}</span>

      <span className="h-3 w-px bg-border" />

      {/* Timer */}
      <span
        className={`text-sm font-bold tabular-nums transition-colors ${
          isUrgent ? 'text-destructive animate-pulse' : 'text-accent'
        }`}
      >
        {timeRemaining}s
      </span>
    </div>
  );
}
