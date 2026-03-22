import { useNavigate } from 'react-router';
import { useCreatePrivateRoom } from '@/hooks/useLobby';
import { Button } from '@/components/ui/Button';
import { GalaxyBackground } from '@/components/background/GalaxyBackground';
import InfoBar from '@/components/ui/InfoBar';
import { Input } from '@/components/ui/input';
import { toast } from 'sonner';
import { useState } from 'react';

export function HomePage() {
  const navigate = useNavigate();
  const createPrivate = useCreatePrivateRoom();
  const [pseudo, setPseudo] = useState<string>('');

  const handleCreatePrivate = async () => {
    if (pseudo == '') {
      toast('Ben alors tu donnes pas de nom?', { position: 'top-left' });
      return;
    }
    if (Math.floor(Math.random() * 3) == 1) {
      toast('Pseudo pas assez cool, réessaye', { position: 'top-left' });
      return;
    }
    try {
      const room = (await createPrivate.mutateAsync({})) ?? 1;

      navigate(`/room/${room.id}`);
    } catch (e) {
      console.log({ e });
      toast('Échec de création de la salle', { position: 'top-left' });
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
        subtitle="Rentre un pseudo pour jouer"
        content={
          <Input
            value={pseudo}
            onChange={(e) => setPseudo(e.target.value ?? '')}
            id="input-demo-api-key"
            placeholder="CoolName"
          />
        }
        actions={
          <Button
            size="lg"
            onClick={handleCreatePrivate}
            disabled={createPrivate.isPending}
            variant={'default'}
          >
            Démarrer
          </Button>
        }
      />
    </GalaxyBackground>
  );
}
