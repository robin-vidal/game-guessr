export default function PlaceholderPanel({
  label,
  description,
}: Readonly<{
  label: string;
  description: string;
}>) {
  return (
    <div className="rounded-[var(--radius)] bg-card/90 backdrop-blur-sm p-4 text-center border border-border">
      <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground mb-1">
        {label}
      </p>
      <p className="text-sm text-foreground">{description}</p>
    </div>
  );
}
