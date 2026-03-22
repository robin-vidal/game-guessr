import { BrowserRouter, Routes, Route } from 'react-router';
import { ProtectedRoute } from './ProtectedRoute';

// Pages
import { LobbyPage } from '@/components/pages/lobby/page';
import { RoomPage } from '@/components/pages/room/page';
import { GamePage } from '@/components/pages/game/page';
import { NotFoundPage } from '@/components/pages/notFound/page';
import { HomePage } from '@/components/pages/home/page';
import { ResultsPage } from '@/components/pages/results/page';

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/lobby" element={<LobbyPage />} />
          <Route path="/room/:roomId" element={<RoomPage />} />
          <Route path="/game/:roomId" element={<GamePage />} />
          <Route path="/results/:roomId" element={<ResultsPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
