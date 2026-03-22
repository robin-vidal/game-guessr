import { useParams, useNavigate } from 'react-router';
import { useGame } from '@/contexts/game/GameContext';
import { Button } from '@/components/ui/Button';
import type { LeaderboardEntry } from '@/types';
import { useAuth } from '@/hooks/useAuth';

export function ResultsPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { leaderboard, disconnectFromRoom } = useGame();

  const handlePlayAgain = () => {
    navigate(`/room/${roomId}`);
  };

  const handleBackToLobby = () => {
    disconnectFromRoom();
    navigate('/lobby');
  };

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-8 bg-background p-8">
      <div className="flex flex-col items-center gap-1">
        <h2 className="text-3xl font-black tracking-tight">Match Over</h2>
        <p className="text-muted-foreground text-sm">Final standings</p>
      </div>

      {/* Leaderboard */}
      <ul className="w-full max-w-sm divide-y divide-border rounded-lg border bg-card">
        {leaderboard.length === 0 && (
          <li className="px-4 py-6 text-center text-sm text-muted-foreground">No results yet…</li>
        )}
        {leaderboard.map((entry) => (
          <LeaderboardRow
            key={entry.userId}
            entry={entry}
            isCurrentUser={entry.userId === user?.id}
          />
        ))}
      </ul>

      <div className="flex gap-3">
        <Button onClick={handlePlayAgain}>Play Again</Button>
        <Button variant="outline" onClick={handleBackToLobby}>
          Back to Lobby
        </Button>
      </div>
    </main>
  );
}

// ─────────────────────────────────────────────
// Leaderboard Row
// ─────────────────────────────────────────────

interface LeaderboardRowProps {
  entry: LeaderboardEntry;
  isCurrentUser: boolean;
}

const rankMedal: Record<number, string> = { 1: '🥇', 2: '🥈', 3: '🥉' };

function LeaderboardRow({ entry, isCurrentUser }: Readonly<LeaderboardRowProps>) {
  return (
    <li className={`flex items-center gap-4 px-4 py-3 ${isCurrentUser ? 'bg-accent/50' : ''}`}>
      <span className="w-6 text-center text-lg">
        {rankMedal[entry.rank] ?? (
          <span className="text-sm font-semibold text-muted-foreground">{entry.rank}</span>
        )}
      </span>
      <span className="flex-1 font-medium">
        {entry.username}
        {isCurrentUser && <span className="ml-2 text-xs text-muted-foreground">(you)</span>}
      </span>
      <span className="text-sm font-bold tabular-nums">{entry.totalScore} pts</span>
    </li>
  );
}
