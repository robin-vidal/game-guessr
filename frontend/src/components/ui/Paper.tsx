import { ReactNode } from "react";

interface PaperProps {
  children?: ReactNode;
}

export default function Paper({ children }: Readonly<PaperProps>) {
  return (
    <div
      style={{
        background: "hsl(var(--background))",
        borderRadius: "var(--radius)",
        padding: "20px 24px",
        display: "flex",
        alignItems: "start",
        flexDirection: "column",
        gap: "16px",
        flexWrap: "wrap",
        maxWidth: "900px",
        width: "100%",
        marginInline: "auto",
        opacity: 0.95,
        border: "solid 1px",
        borderColor: "var(--muted-foreground)",
      }}
    >
      {children}
    </div>
  );
}
