export default function Title({ text }: Readonly<{ text: string }>) {
  return (
    <h1
      className="text-7xl font-black tracking-tight"
      style={{
        fontFamily: "'Fredoka One', 'Boogaloo', system-ui, sans-serif",
        background:
          "linear-gradient(135deg, #fff 0%, #a8d8ff 40%, #c084fc 70%, #f472b6 100%)",
        WebkitBackgroundClip: "text",
        WebkitTextFillColor: "transparent",
        backgroundClip: "text",
        filter:
          "drop-shadow(0 0 30px rgba(139,92,246,0.8)) drop-shadow(0 0 60px rgba(59,130,246,0.5))",
        letterSpacing: "-0.02em",
        lineHeight: 1.1,
      }}
    >
      {text}
    </h1>
  );
}
