import { Input } from '@/components/ui/input';
import PlaceholderPanel from '../PlaceholderPanel';
import { Button } from '@/components/ui/Button';
import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { submitGuessMutation } from '@/client/game-service/@tanstack/react-query.gen';
import { Play } from 'lucide-react';
import { defaultConfig } from '@/client/config';
import { useAuth } from '@/hooks/useAuth';

export default function GuessLevelInput({ roomId }: Readonly<{ roomId: string }>) {
  const [value, setValue] = useState<string>('');
  const { user } = useAuth();

  const { mutateAsync: sendGuess, isPending } = useMutation(submitGuessMutation());

  if (!user) return null;

  async function onSumbit() {
    const res = await sendGuess({
      body: { playerId: user?.id ?? '', phase: 'LEVEL', textAnswer: value },
      path: { code: roomId },
      ...defaultConfig,
    });
    console.log({ res });
  }

  return (
    <PlaceholderPanel
      label="Phase 2"
      description="Quel niveau est-ce?"
      input={
        <div style={{ display: 'flex', flexWrap: 'nowrap', gap: 8 }}>
          <Input
            id="input-demo-api-key"
            value={value}
            onChange={(e) => setValue(e.target.value)}
            placeholder="Nom du niveau"
            disabled={isPending}
          />
          <Button type="submit" onClick={onSumbit} size={'icon'}>
            <Play />
          </Button>
        </div>
      }
    />
  );
}
