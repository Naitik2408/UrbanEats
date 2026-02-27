import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { MainLayout, AuthLayout } from '../components/layout';
import { Loader } from '../components/ui';
import { ProtectedRoute } from './ProtectedRoute';

// Lazy load pages for code splitting
const LandingPage = lazy(() =>
  import('../pages/LandingPage').then((m) => ({ default: m.LandingPage }))
);
const AuthPage = lazy(() =>
  import('../pages/AuthPage').then((m) => ({ default: m.AuthPage }))
);
const AdminDashboard = lazy(() =>
  import('../pages/AdminDashboard').then((m) => ({ default: m.AdminDashboard }))
);
const CustomerDashboard = lazy(() =>
  import('../pages/CustomerDashboard').then((m) => ({
    default: m.CustomerDashboard,
  }))
);
const NotFoundPage = lazy(() =>
  import('../pages/NotFoundPage').then((m) => ({ default: m.NotFoundPage }))
);

/**
 * Application router configuration.
 * 
 * Features:
 * - Lazy loading for all pages
 * - Protected routes with role-based access
 * - Nested layouts (MainLayout, AuthLayout)
 * - 404 fallback route
 */
const router = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: (
          <Suspense fallback={<Loader />}>
            <LandingPage />
          </Suspense>
        ),
      },
      {
        path: 'admin',
        element: <ProtectedRoute allowedRoles={['ADMIN']} />,
        children: [
          {
            index: true,
            element: (
              <Suspense fallback={<Loader />}>
                <AdminDashboard />
              </Suspense>
            ),
          },
        ],
      },
      {
        path: 'customer',
        element: <ProtectedRoute allowedRoles={['CUSTOMER']} />,
        children: [
          {
            index: true,
            element: (
              <Suspense fallback={<Loader />}>
                <CustomerDashboard />
              </Suspense>
            ),
          },
        ],
      },
    ],
  },
  {
    path: '/auth',
    element: <AuthLayout />,
    children: [
      {
        index: true,
        element: (
          <Suspense fallback={<Loader />}>
            <AuthPage />
          </Suspense>
        ),
      },
    ],
  },
  {
    path: '*',
    element: (
      <Suspense fallback={<Loader />}>
        <NotFoundPage />
      </Suspense>
    ),
  },
]);

/**
 * AppRouter component - Application routing provider.
 */
export const AppRouter: React.FC = () => {
  return <RouterProvider router={router} />;
};
