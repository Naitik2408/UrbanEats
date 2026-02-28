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
 * - Redirects unauthenticated users to appropriate login page
 * - Redirects users to their role-specific dashboard if accessing wrong route
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  allowedRoles = [],
}) => {
  const { isAuthenticated, role } = useAuthStore();

  // Not authenticated - redirect to customer login page
  if (!isAuthenticated) {
    return <Navigate to="/auth/customer-login" replace />;
  }

  // Authenticated but no role restrictions - allow access
  if (allowedRoles.length === 0) {
    return <Outlet />;
  }

  // Check if user's role is in allowed roles
  if (role && allowedRoles.includes(role)) {
    return <Outlet />;
  }

  // Authenticated but wrong role - redirect to appropriate dashboard
  if (role === 'ADMIN') {
    return <Navigate to="/admin" replace />;
  }
  if (role === 'CUSTOMER') {
    return <Navigate to="/customer" replace />;
  }

  // Fallback to home page
  return <Navigate to="/" replace />;
};
