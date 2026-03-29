export default function PlaceholderPanel({
  label,
  description,
  input,
}: Readonly<{
  label: string;
  description: string;
  input?: React.ReactNode;
}>) {
  return (
    <div className="rounded-[var(--radius)] bg-card/90 backdrop-blur-sm p-4 text-center border border-border gap-4 flex flex-col">
      <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground mb-1">
        {label}
      </p>
      <p className="text-sm text-foreground">{description}</p>
      {input}
    </div>
  );
}
