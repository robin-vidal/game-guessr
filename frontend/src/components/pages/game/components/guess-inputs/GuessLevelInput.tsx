import { Command } from 'cmdk';
import PlaceholderPanel from '../PlaceholderPanel';
import { Button } from '@/components/ui/Button';
import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { submitGuessMutation } from '@/client/game-service/@tanstack/react-query.gen';
import { Play } from 'lucide-react';
import { defaultConfig } from '@/client/config';
import { useAuth } from '@/hooks/useAuth';

export default function GuessLevelInput({ roomId }: Readonly<{ roomId: string }>) {
  const [value, setValue] = useState('');
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [open, setOpen] = useState(false);
  const { user } = useAuth();

  const { mutateAsync: sendGuess, isPending, isSuccess } = useMutation(submitGuessMutation());

  if (!user) return null;

  async function fetchSuggestions(query: string) {
    if (query.length < 4) {
      setSuggestions([]);
      return;
    }
    try {
      const baseUrl = defaultConfig.baseUrl ?? '';
      const res = await fetch(`${baseUrl}/api/v1/levels/autocomplete?q=${encodeURIComponent(query)}`);
      const data: string[] = await res.json();
      setSuggestions(data);
    } catch {
      setSuggestions([]);
    }
  }

  const showSuggestions = open && suggestions.length > 0;

  async function onSubmit() {
    if (!value.trim()) return;
    await sendGuess({
      body: { playerId: user?.id ?? '', phase: 'LEVEL', textAnswer: value },
      path: { code: roomId },
      ...defaultConfig,
    });
  }

  if (isSuccess) {
    return (
      <PlaceholderPanel label="Phase 2" description="Réponse envoyée !" />
    );
  }

  return (
    <PlaceholderPanel
      label="Phase 2"
      description="Quel niveau est-ce?"
      input={
        <div className="relative flex flex-nowrap gap-2">
          <div className="relative flex-1">
            <Command shouldFilter={false}>
              <Command.Input
                value={value}
                onValueChange={(v) => {
                  setValue(v);
                  fetchSuggestions(v);
                  setOpen(v.length >= 4);
                }}
                onFocus={() => value.length >= 4 && setOpen(true)}
                onBlur={() => setTimeout(() => setOpen(false), 150)}
                placeholder="Nom du niveau"
                disabled={isPending}
                className="flex h-10 w-full border border-input bg-secondary text-foreground placeholder:text-muted-foreground rounded-[var(--radius)] px-4 py-2 text-sm ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              />
              {showSuggestions && (
                <Command.List className="absolute bottom-full mb-1 w-full z-10 rounded-[var(--radius)] border border-border bg-card shadow-lg overflow-hidden max-h-48 overflow-y-auto">
                  {suggestions.slice(0, 8).map((name) => (
                    <Command.Item
                      key={name}
                      value={name}
                      onSelect={(v) => {
                        setValue(v);
                        setSuggestions([]);
                        setOpen(false);
                      }}
                      className="px-4 py-2 text-sm text-left text-foreground hover:bg-secondary cursor-pointer transition-colors data-[selected=true]:bg-secondary"
                    >
                      {name}
                    </Command.Item>
                  ))}
                  <Command.Empty className="px-4 py-2 text-sm text-muted-foreground">
                    Aucun résultat
                  </Command.Empty>
                </Command.List>
              )}
            </Command>
          </div>
          <Button type="submit" onClick={onSubmit} size="icon" disabled={isPending || !value.trim()}>
            <Play />
          </Button>
        </div>
      }
    />
  );
}
