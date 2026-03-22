import { GamePhase } from '@/types';

interface HUDProps {
  roundNumber: number;
  totalRounds: number;
  phase: GamePhase | null;
  timeRemaining: number;
}

export default function GameHUD({
  roundNumber,
  totalRounds,
  phase,
  timeRemaining,
}: Readonly<HUDProps>) {
  const phaseLabel: Record<GamePhase, string> = {
    [GamePhase.WAITING]: 'Waiting',
    [GamePhase.EXPLORING]: 'Explore!',
    [GamePhase.GUESSING_GAME]: 'Phase 1 — Which game?',
    [GamePhase.GUESSING_LEVEL]: 'Phase 2 — Which level?',
    [GamePhase.GUESSING_SPOT]: 'Phase 3 — Where exactly?',
    [GamePhase.ROUND_RESULTS]: 'Round over',
    [GamePhase.MATCH_FINISHED]: 'Match finished',
  };

  const isUrgent = timeRemaining <= 10;

  return (
    <div className="absolute top-4 left-1/2 -translate-x-1/2 flex items-center gap-4 rounded-full bg-card/80 border border-border backdrop-blur-sm px-5 py-2 text-foreground">
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
          isUrgent ? 'text-destructive' : 'text-accent'
        }`}
      >
        {timeRemaining}s
      </span>
    </div>
  );
}
