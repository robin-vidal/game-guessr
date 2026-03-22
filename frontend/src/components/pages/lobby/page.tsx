import { useNavigate } from 'react-router';
import { useJoinPublicRoom, useCreatePrivateRoom } from '@/hooks/useLobby';
import { Button } from '@/components/ui/Button';
import { useAuth } from '@/hooks/useAuth';
import { GalaxyBackground } from '@/components/background/GalaxyBackground';

export function LobbyPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const joinPublic = useJoinPublicRoom();
  const createPrivate = useCreatePrivateRoom();

  const handleJoinPublic = async () => {
    const room = await joinPublic.mutateAsync();
    navigate(`/room/${room.id}`);
  };

  const handleCreatePrivate = async () => {
    const room = await createPrivate.mutateAsync({});
    navigate(`/room/${room.id}`);
  };

  return (
    <GalaxyBackground>
      <div className="flex flex-col items-center gap-1">
        <h1
          className="text-7xl font-black tracking-tight"
          style={{
            fontFamily: "'Fredoka One', 'Boogaloo', system-ui, sans-serif",
            background: 'linear-gradient(135deg, #fff 0%, #a8d8ff 40%, #c084fc 70%, #f472b6 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
            filter:
              'drop-shadow(0 0 30px rgba(139,92,246,0.8)) drop-shadow(0 0 60px rgba(59,130,246,0.5))',
            letterSpacing: '-0.02em',
            lineHeight: 1.1,
          }}
        >
          GameGuessr
        </h1>
        <p className="text-muted-foreground text-sm">Welcome, {user?.displayName}</p>
      </div>

      <div className="flex flex-col gap-3 w-full max-w-xs">
        <Button size="lg" onClick={handleJoinPublic} disabled={joinPublic.isPending}>
          {joinPublic.isPending ? 'Finding a match…' : 'Play Public'}
        </Button>
        <Button
          size="lg"
          variant="outline"
          onClick={handleCreatePrivate}
          disabled={createPrivate.isPending}
        >
          Create Private Room
        </Button>
      </div>

      <Button variant="ghost" size="sm" onClick={logout}>
        Sign out
      </Button>
    </GalaxyBackground>
  );
}
