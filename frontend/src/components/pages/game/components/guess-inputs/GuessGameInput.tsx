import { Input } from '@/components/ui/input';
import PlaceholderPanel from '../PlaceholderPanel';
import { Button } from '@/components/ui/Button';
import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { submitGuessMutation } from '@/client/game-service/@tanstack/react-query.gen';
import { Play } from 'lucide-react';
import { defaultConfig } from '@/client/config';
import { useAuth } from '@/hooks/useAuth';
import { toast } from 'sonner';

export default function GuessGameInput({ roomId }: Readonly<{ roomId: string }>) {
  const [value, setValue] = useState<string>('');
  const [hasSubmitted, setHasSumbitted] = useState(false);
  const { user } = useAuth();

  const { mutateAsync: sendGuess, isPending } = useMutation(submitGuessMutation());

  if (!user) return null;

  async function onSubmit() {
    try{
      await sendGuess({
        body: { playerId: user?.id ?? '', phase: 'GAME', textAnswer: value },
        path: { code: roomId },
        ...defaultConfig,
      });
      toast(`La soumission a été envoyé`, { position: 'top-left' });
      setHasSumbitted(true);
    }
    catch(e){
      console.log(e)
      toast(`Échec de la soumission`, { position: 'top-left' });
    }
  }

  return (
    <PlaceholderPanel
      label="Phase 1"
      description="De quel jeu est-ce la carte ?"
      input={
        <div style={{ display: 'flex', flexWrap: 'nowrap', gap: 8 }}>
          <Input
            id="input-demo-api-key"
            value={value}
            onChange={(e) => setValue(e.target.value)}
            placeholder="Nom du jeu"
            disabled={isPending || hasSubmitted}
          />
          <Button type="submit" onClick={onSubmit} size={'icon'}>
            <Play />
          </Button>
        </div>
      }
    />
  );
}
