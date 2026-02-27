import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import type { UserRole } from '../types';

interface ProtectedRouteProps {
  allowedRoles?: UserRole[];
}

/**
 * ProtectedRoute component for role-based access control.
 * 
 * Features:
 * - Checks authentication status
 * - Validates user role against allowed roles
 * - Redirects unauthenticated users to /auth
 * - Redirects unauthorized users to /
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  allowedRoles = [],
}) => {
  const { isAuthenticated, role } = useAuthStore();

  // Not authenticated - redirect to auth page
  if (!isAuthenticated) {
    return <Navigate to="/auth" replace />;
  }

  // Authenticated but no role restrictions - allow access
  if (allowedRoles.length === 0) {
    return <Outlet />;
  }

  // Check if user's role is in allowed roles
  if (role && allowedRoles.includes(role)) {
    return <Outlet />;
  }

  // Authenticated but not authorized - redirect to home
  return <Navigate to="/" replace />;
};
