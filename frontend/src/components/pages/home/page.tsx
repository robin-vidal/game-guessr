import { useNavigate } from 'react-router';
import { Button } from '@/components/ui/Button';
import { GalaxyBackground } from '@/components/background/GalaxyBackground';
import InfoBar from '@/components/ui/InfoBar';
import { toast } from 'sonner';
import { useState } from 'react';
import { createRoomMutation } from '@/client/lobby-service/@tanstack/react-query.gen';
import { useMutation } from '@tanstack/react-query';

import { defaultConfig } from '@/client/config';
import { useAuth } from '@/hooks/useAuth';

export function HomePage() {
  const navigate = useNavigate();
  const { mutateAsync: createRoom, isPending } = useMutation(createRoomMutation());
  const { user } = useAuth();

  const [isPrivate, setIsPrivate] = useState<boolean>(false);

  if (!user) {
    navigate('/login');
    return null;
  }

  const handleCreateRoom = async () => {
    try {
      const room = await createRoom({
        body: { hostId: user.id, isPrivate },
        ...defaultConfig,
      });
      navigate(`/room/${room.roomCode}`);
    } catch (e) {
      console.log({ e });
      toast(`Échec de création de la salle`, { position: 'top-left' });
    }
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
      </div>

      <div className="flex flex-col gap-3 w-full max-w-xs"></div>

      <InfoBar
        title="Lancer une partie"
        subtitle="Joue avec tes amis ou des gens du monde entier"
        content={
          <div className="flex gap-2 w-full">
            <Button
              variant={!isPrivate ? 'default' : 'outline'}
              className="flex-1"
              onClick={() => setIsPrivate(false)}
            >
              Salle publique
            </Button>
            <Button
              variant={isPrivate ? 'default' : 'outline'}
              className="flex-1"
              onClick={() => setIsPrivate(true)}
            >
              Salle privée
            </Button>
          </div>
        }
        actions={
          <Button size="lg" onClick={handleCreateRoom} disabled={isPending} variant={'default'}>
            Démarrer
          </Button>
        }
      />
    </GalaxyBackground>
  );
}
