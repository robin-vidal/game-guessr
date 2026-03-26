import { useNavigate } from 'react-router';
import { Button } from '@/components/ui/Button';
import { GalaxyBackground } from '@/components/background/GalaxyBackground';
import InfoBar from '@/components/ui/InfoBar';
import { Input } from '@/components/ui/input';
import { toast } from 'sonner';
import { useState } from 'react';

import { useAuth } from '@/hooks/useAuth';

export function RegisterPage() {
  const navigate = useNavigate();
  const { user, isLoading, register } = useAuth();

  const [username, setUsername] = useState<string>('');
  const [password, setPassword] = useState<string>('');

  if (user) {
    navigate('/');
    return;
  }

  const handleRegister = async () => {
    if (username == '') {
      toast('Ben alors tu donnes pas de nom?', { position: 'top-left' });
      return;
    }
    if (Math.floor(Math.random() * 3) == 1) {
      toast('Pseudo pas assez cool, réessaye', { position: 'top-left' });
      return;
    }
    try {
      await register({ username, password });

      navigate(`/`);
    } catch (e) {
      if ((e as { detail: string }).detail) {
        toast(`${(e as { detail: string }).detail}`, {
          position: 'top-left',
        });
      } else {
        toast(`Échec de création de compte `, {
          position: 'top-left',
        });
      }
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
          Bienvenue
        </h1>
      </div>

      <div className="flex flex-col gap-3 w-full max-w-xs"></div>

      <InfoBar
        title="Crée un compte pour jouer"
        subtitle=""
        content={
          <>
            <Input
              value={username}
              onChange={(e) => setUsername(e.target.value ?? '')}
              id="input-demo-api-key"
              placeholder="Pseudo cool"
              disabled={isLoading}
            />
            <Input
              value={password}
              onChange={(e) => setPassword(e.target.value ?? '')}
              id="input-demo-api-key"
              type="password"
              placeholder="Mot de passe sécurisé"
              disabled={isLoading}
            />
            <a style={{ textDecoration: 'underline', fontSize: '1rem' }} href="/register">
              Déjà un compte?
            </a>
          </>
        }
        actions={
          <Button size="lg" onClick={handleRegister} disabled={isLoading} variant={'default'}>
            Se connecter
          </Button>
        }
      />
    </GalaxyBackground>
  );
}
