import { useParams, useNavigate } from 'react-router';
import { useEffect } from 'react';

import { Button } from '@/components/ui/Button';
import { useAuth } from '@/hooks/useAuth';
import { GalaxyBackground } from '@/components/background/GalaxyBackground';
import Paper from '@/components/ui/Paper';
import { House, Copy } from 'lucide-react';
import Container from '@/components/ui/Container';
import { toast } from 'sonner';
import { useMutation, useQuery } from '@tanstack/react-query';
import { getRoomOptions } from '@/client/lobby-service/@tanstack/react-query.gen';
import { startMatchMutation } from '@/client/game-service/@tanstack/react-query.gen';
import { defaultConfig } from '@/client/config';

export function RoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const { data: room, isPending } = useQuery(
    getRoomOptions({ path: { code: roomId! }, ...defaultConfig })
  );

  const { mutateAsync: startMatch, isPending: isStartMatchPending } =
    useMutation(startMatchMutation());

  // Navigate to game when match starts
  useEffect(() => {
    if (room?.status && room?.status != 'OPEN') {
      navigate(`/game/${roomId}`);
    }
  }, [room?.status, navigate, roomId]);

  if (!roomId) {
    navigate(`/`);
    return;
  }
  if (isPending) {
    return (
      <GalaxyBackground>
        <div className="flex h-screen items-center justify-center">
          <span className="text-muted-foreground animate-pulse text-sm">
            Chargement de la salle…
          </span>
        </div>
      </GalaxyBackground>
    );
  }

  if (!room) {
    navigate(`/`);
    return null;
  }

  const isHost = room.hostId === user?.id;
  const inviteUrl = `${window.location.origin}/room/${roomId}`;

  const onShare = () => {
    navigator.clipboard.writeText(inviteUrl);
    toast("L'url a été copiée dans le presse-papier", { position: 'top-left' });
  };

  const onGameStart = async () => {
    await startMatch({
      body: {
        hostId: room.hostId!,
        playerIds: (room.players ?? []).map((p) => p.playerId ?? ''),
      },
      path: { code: roomId! },
      ...defaultConfig,
    });
    navigate(`/game/${roomId}`);
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
            <ul className="w-full divide-y divide-border rounded-lg border bg-card">
              {room?.players?.map((p) => (
                <li key={p.playerId} className="flex items-center justify-between px-4 py-3">
                  <span className="font-medium">{p.displayName}</span>
                  {p.playerId == room.hostId && (
                    <span className="text-xs text-muted-foreground">Hôte</span>
                  )}
                </li>
              ))}
            </ul>
            {!isHost && (
              <p className="text-muted-foreground text-sm animate-pulse">
                {"En attente que l'hôte démarre…"}
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
            disabled={isStartMatchPending || (room?.players?.length ?? 0) < 1 || !isHost}
          >
            Démarrer
          </Button>
        </div>
      </Container>
    </GalaxyBackground>
  );
}
