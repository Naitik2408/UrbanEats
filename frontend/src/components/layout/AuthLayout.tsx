import { Outlet } from 'react-router-dom';

/**
 * Auth layout wrapper for authentication pages.
 * 
 * Features:
 * - Centered content
 * - Clean minimal design
 * - Responsive
 * - Renders child routes via Outlet
 */
export const AuthLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-gray-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        {/* Logo */}
        <h1 className="text-center text-4xl font-bold text-blue-600 mb-8">
          UrbanEats
        </h1>
      </div>

      {/* Auth Form Container */}
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow-lg sm:rounded-lg sm:px-10">
          <Outlet />
        </div>
      </div>

      {/* Footer */}
      <footer className="mt-8">
        <p className="text-center text-sm text-gray-600">
          © {new Date().getFullYear()} UrbanEats. All rights reserved.
        </p>
      </footer>
    </div>
  );
};
