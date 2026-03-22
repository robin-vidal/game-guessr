import { useParams, useNavigate } from 'react-router';
import { useEffect } from 'react';
import { useRoom, useStartMatch } from '@/hooks/useLobby';
import { useGame } from '@/contexts/game/GameContext';
import { useWebSocket } from '@/hooks/useWebSocket';
import { Button } from '@/components/ui/Button';
import { GamePhase } from '@/types';
import { useAuth } from '@/hooks/useAuth';
import { GalaxyBackground } from '@/components/background/GalaxyBackground';
import Paper from '@/components/ui/Paper';
import { House, Copy } from 'lucide-react';
import Container from '@/components/ui/Container';
import { toast } from 'sonner';

export function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { data: room, isLoading } = useRoom(roomId!);

  const startMatch = useStartMatch(roomId!);
  const { connectToRoom, currentPhase } = useGame();

  // Connect to WebSocket when we enter the room
  useEffect(() => {
    if (roomId) connectToRoom(roomId);
  }, [roomId, connectToRoom]);

  // Navigate to game when match starts
  useEffect(() => {
    if (currentPhase && currentPhase !== GamePhase.WAITING) {
      navigate(`/game/${roomId}`);
    }
  }, [currentPhase, navigate, roomId]);

  // Also listen to the raw WS event as a fallback
  useWebSocket('MATCH_STARTED', () => {
    navigate(`/game/${roomId}`);
  });

  if (!roomId) {
    navigate(`/home/`);
  }
  if (isLoading || !room) {
    return (
      <GalaxyBackground>
        <div className="flex h-screen items-center justify-center">
          <span className="text-muted-foreground animate-pulse text-sm">Loading room…</span>
        </div>
      </GalaxyBackground>
    );
  }

  console.log({ room });
  const isHost = room.hostId === user?.id;
  const inviteUrl = `${window.location.origin}/room/${roomId}`;

  const onShare = () => {
    navigator.clipboard.writeText(inviteUrl);
    toast("L'url a été copiée dans le presse-papier", { position: 'top-left' });
  };

  const onGameStart = async () => {
    await startMatch.mutateAsync();
  };

  return (
    <GalaxyBackground>
      <Button
        variant="secondary"
        size="icon"
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          margin: '2rem',
          padding: 4,
        }}
        onClick={() => navigate('/')}
      >
        <House />
      </Button>
      <Container>
        <Paper>
          <>
            <h2 className="text-2xl font-bold " style={{ textAlign: 'left', width: '100%' }}>
              Liste des joueurs
            </h2>
            <p className="text-muted-foreground text-sm">
              {room?.players?.length ?? 0} joueur
              {(room?.players?.length ?? 0) == 1 ? '' : 's'} en ligne
            </p>
            <ul
              className="w-full max-w-xs divide-y divide-border rounded-lg border bg-card"
              style={{ width: '100%' }}
            >
              {room?.players?.map((p) => (
                <li key={p.userId} className="flex items-center justify-between px-4 py-3">
                  <span className="font-medium">{p.username}</span>
                  {p.isHost && <span className="text-xs text-muted-foreground">Host</span>}
                </li>
              ))}
            </ul>
            {!isHost && (
              <p className="text-muted-foreground text-sm animate-pulse">
                Waiting for host to start…
              </p>
            )}
          </>
        </Paper>
        <div
          style={{
            gap: '16px',
            display: 'flex',
            justifyContent: 'space-between',
            width: '100%',
          }}
        >
          <Button variant="secondary" style={{ gap: 8 }} onClick={onShare}>
            Partager
            <Copy size={16} />
          </Button>
          <Button
            onClick={onGameStart}
            disabled={startMatch?.isPending || (room?.players?.length ?? 0) < 1 || !isHost}
          >
            Démarrer
          </Button>
        </div>
      </Container>
    </GalaxyBackground>
  );
}
