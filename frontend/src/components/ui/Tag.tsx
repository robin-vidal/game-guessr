import { ReactNode } from "react";

interface TagProps {
  children: ReactNode;
}

export function Tag({ children }: Readonly<TagProps>) {
  return (
    <span
      style={{
        background: "hsl(var(--primary))",
        color: "hsl(var(--primary-foreground))",
        borderRadius: "9999px",
        padding: "4px 14px",
        fontSize: "12px",
        fontWeight: 500,
        whiteSpace: "nowrap",
        flexShrink: 0,
      }}
    >
      {children}
    </span>
  );
}