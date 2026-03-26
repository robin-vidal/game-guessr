import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router';
import { GalaxyBackground } from '@/components/background/GalaxyBackground';
import { useAuth } from '@/hooks/useAuth';

export function LogoutPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const hasRun = useRef(false);

  useEffect(() => {
    // Strict Mode guard — ensures logout fires exactly once
    if (hasRun.current) return;
    hasRun.current = true;

    logout()
      .catch(console.error)
      .finally(() => navigate('/login', { replace: true }));
  }, [logout, navigate]);

  return (
    <GalaxyBackground>
      <div className="flex flex-col items-center gap-6">
        <h1
          className="text-5xl font-black tracking-tight"
          style={{
            fontFamily: "'Fredoka One', 'Boogaloo', system-ui, sans-serif",
            background: 'linear-gradient(135deg, #fff 0%, #a8d8ff 40%, #c084fc 70%, #f472b6 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
            filter:
              'drop-shadow(0 0 30px rgba(139,92,246,0.8)) drop-shadow(0 0 60px rgba(59,130,246,0.5))',
          }}
        >
          À bientôt !
        </h1>

        <div className="flex items-center gap-3 text-white/60 text-sm">Déconnexion en cours…</div>
      </div>
    </GalaxyBackground>
  );
}
