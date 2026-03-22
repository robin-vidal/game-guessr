import { ReactNode } from "react";

interface ContainerProps {
  children?: ReactNode;
}

export default function Container({ children }: Readonly<ContainerProps>) {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "start",
        flexDirection: "column",
        gap: "16px",
        flexWrap: "wrap",
        maxWidth: "900px",
        width: "100%",
        marginInline: "auto",
      }}
    >
      {children}
    </div>
  );
}
