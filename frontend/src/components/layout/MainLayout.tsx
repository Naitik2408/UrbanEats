import { Outlet, Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

/**
 * Main layout wrapper for authenticated pages.
 * 
 * Features:
 * - Navigation header with auth state
 * - Responsive container
 * - Footer
 * - Renders child routes via Outlet
 */
export const MainLayout: React.FC = () => {
  const { isAuthenticated, role, logout } = useAuthStore();

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <Link to="/" className="flex items-center">
              <span className="text-2xl font-bold text-blue-600">UrbanEats</span>
            </Link>

            {/* Navigation */}
            <nav className="flex items-center gap-6">
              {isAuthenticated ? (
                <>
                  <span className="text-sm text-gray-600">
                    Role: <span className="font-medium">{role}</span>
                  </span>
                  {role === 'ADMIN' && (
                    <Link
                      to="/admin"
                      className="text-gray-700 hover:text-blue-600 transition-colors"
                    >
                      Admin Dashboard
                    </Link>
                  )}
                  {role === 'CUSTOMER' && (
                    <Link
                      to="/customer"
                      className="text-gray-700 hover:text-blue-600 transition-colors"
                    >
                      My Orders
                    </Link>
                  )}
                  <button
                    onClick={logout}
                    className="px-4 py-2 text-sm text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors"
                  >
                    Logout
                  </button>
                </>
              ) : (
                <Link
                  to="/auth"
                  className="px-4 py-2 text-sm text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Sign In
                </Link>
              )}
            </nav>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Outlet />
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <p className="text-center text-sm text-gray-600">
            © {new Date().getFullYear()} UrbanEats. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
};
