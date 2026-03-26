import { GalaxyBackground } from '../background/GalaxyBackground';

export default function LoadingScreen() {
  return (
    <GalaxyBackground>
      <span className="text-muted-foreground text-sm animate-pulse">Chargement</span>
    </GalaxyBackground>
  );
}
