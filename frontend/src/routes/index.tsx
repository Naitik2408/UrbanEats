import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { MainLayout, AuthLayout } from '../components/layout';
import { Loader } from '../components/ui';
import { ProtectedRoute } from './ProtectedRoute';

// Lazy load pages for code splitting
const LandingPage = lazy(() =>
  import('../pages/LandingPage').then((m) => ({ default: m.LandingPage }))
);
const CustomerLoginPage = lazy(() =>
  import('../pages/auth/CustomerLoginPage')
);
const OtpVerificationPage = lazy(() =>
  import('../pages/auth/OtpVerificationPage')
);
const AdminLoginPage = lazy(() =>
  import('../pages/auth/AdminLoginPage')
);
const AdminDashboard = lazy(() =>
  import('../pages/AdminDashboard').then((m) => ({ default: m.AdminDashboard }))
);
const CustomerDashboard = lazy(() =>
  import('../pages/CustomerDashboard').then((m) => ({
    default: m.CustomerDashboard,
  }))
);
const CitySelectionPage = lazy(() =>
  import('../pages/customer/CitySelectionPage')
);
const RestaurantListPage = lazy(() =>
  import('../pages/customer/RestaurantListPage')
);
const ItemListPage = lazy(() =>
  import('../pages/customer/ItemListPage')
);
const CartPage = lazy(() =>
  import('../pages/customer/CartPage')
);
const CheckoutPage = lazy(() =>
  import('../pages/customer/CheckoutPage')
);
const OrdersPage = lazy(() =>
  import('../pages/customer/OrdersPage')
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
          {
            path: 'cities',
            element: (
              <Suspense fallback={<Loader />}>
                <CitySelectionPage />
              </Suspense>
            ),
          },
          {
            path: 'restaurants',
            element: (
              <Suspense fallback={<Loader />}>
                <RestaurantListPage />
              </Suspense>
            ),
          },
          {
            path: 'items',
            element: (
              <Suspense fallback={<Loader />}>
                <ItemListPage />
              </Suspense>
            ),
          },
          {
            path: 'cart',
            element: (
              <Suspense fallback={<Loader />}>
                <CartPage />
              </Suspense>
            ),
          },
          {
            path: 'checkout',
            element: (
              <Suspense fallback={<Loader />}>
                <CheckoutPage />
              </Suspense>
            ),
          },
          {
            path: 'orders',
            element: (
              <Suspense fallback={<Loader />}>
                <OrdersPage />
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
        path: 'customer-login',
        element: (
          <Suspense fallback={<Loader />}>
            <CustomerLoginPage />
          </Suspense>
        ),
      },
      {
        path: 'verify-otp',
        element: (
          <Suspense fallback={<Loader />}>
            <OtpVerificationPage />
          </Suspense>
        ),
      },
      {
        path: 'admin-login',
        element: (
          <Suspense fallback={<Loader />}>
            <AdminLoginPage />
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
