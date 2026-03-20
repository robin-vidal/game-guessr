import { BrowserRouter, Routes, Route } from 'react-router';
import { ProtectedRoute } from './ProtectedRoute';

import { NotFoundPage } from '@/components/pages/notFound/page';
import { HomePage } from '@/components/pages/home/page';
export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<HomePage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
