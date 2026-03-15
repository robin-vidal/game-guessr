function App() {
  const iframeUrl = import.meta.env.NOCLIP_FRONTEND_URL || 'http://localhost:8000';

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4">
      <h1 className="text-4xl font-bold mb-8">GameGuessr v0.1</h1>
      <iframe
        src={iframeUrl}
        className="w-[66vw] max-w-[66vw] h-[37.125vw] max-h-[66vh] aspect-video"
        title="Game"
      />
    </div>
  );
}

export default App;
