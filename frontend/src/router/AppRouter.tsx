import { BrowserRouter, Routes, Route } from 'react-router';
import { ProtectedRoute } from './ProtectedRoute';

// Pages
import { RoomPage } from '@/components/pages/room/page';
import { GamePage } from '@/components/pages/game/page';
import { NotFoundPage } from '@/components/pages/notFound/page';
import { HomePage } from '@/components/pages/home/page';
import { LoginPage } from '@/components/pages/login/page';
import { RegisterPage } from '@/components/pages/register/page';
import { LogoutPage } from '@/components/pages/logout/page';

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/room/:roomId" element={<RoomPage />} />
          <Route path="/game/:roomId" element={<GamePage />} />
          <Route path="/logout" element={<LogoutPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
