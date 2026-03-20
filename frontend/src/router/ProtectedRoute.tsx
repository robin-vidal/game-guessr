import { useAuth } from "@/hooks/useAuth";
import { Navigate, Outlet, useLocation } from "react-router";

/**
 * Wraps routes that require authentication.
 * Preserves the intended destination so we can redirect back after login.
 */
export function ProtectedRoute() {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    // Avoid flashing the login page while the token is being validated
    return (
      <div className="flex h-screen items-center justify-center">
        <span className="text-muted-foreground text-sm animate-pulse">
          Loading…
        </span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}
