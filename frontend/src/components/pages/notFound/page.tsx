import { useNavigate } from 'react-router';
import { Button } from '@/components/ui/Button';
import Title from '@/components/ui/Title';
import { GalaxyBackground } from '@/components/background/GalaxyBackground';

export function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <GalaxyBackground>
      <div className="flex flex-col gap-1">
        <Title text={'404'} />
        <p className="text-muted-foreground text-sm text-center" style={{ color: 'white' }}>
          {"Tu t'es perdu ?"}
        </p>
      </div>
      <Button onClick={() => navigate('/')}>Retour accueil</Button>
    </GalaxyBackground>
  );
}
