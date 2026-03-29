import { ReactNode } from 'react';

// ─── Subcomponents ────────────────────────────────────────────────────────────

interface InfoBarTitleProps {
  title: string;
  subtitle: string;
}

export function InfoBarTitle({ title, subtitle }: Readonly<InfoBarTitleProps>) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
      <span
        style={{
          fontSize: '20px',
          fontWeight: 500,
          color: 'hsl(var(--foreground))',
          lineHeight: 1.2,
        }}
      >
        {title}
      </span>
      <span
        style={{
          fontSize: '13px',
          color: 'hsl(var(--secondary-foreground))',
          lineHeight: 1.4,
        }}
      >
        {subtitle}
      </span>
    </div>
  );
}

// ─── InfoBar ──────────────────────────────────────────────────────────────────

interface InfoBarProps {
  title?: string;
  subtitle?: string;
  content: ReactNode;
  actions: ReactNode;
}

export function InfoBar({
  title = 'GameGuessr',
  subtitle = 'Guess the game from a single screenshot',
  content = <></>,
  actions = <></>,
}: Readonly<InfoBarProps>) {
  return (
    <div
      style={{
        background: 'hsl(var(--background))',
        borderRadius: 'var(--radius)',
        padding: '20px 24px',
        display: 'flex',
        alignItems: 'center',
        gap: '16px',
        flexWrap: 'wrap',
      }}
    >
      <InfoBarTitle title={title} subtitle={subtitle} />
      {content}

      <div
        style={{
          marginLeft: 'auto',
          display: 'flex',
          gap: '8px',
          flexShrink: 0,
        }}
      >
        {actions}
      </div>
    </div>
  );
}

export default InfoBar;
