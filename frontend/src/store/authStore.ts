import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { setAuthHooks } from '../api/axios';

/**
 * User role types matching backend
 */
export type UserRole = 'ADMIN' | 'CUSTOMER';

/**
 * Auth store state interface
 */
interface AuthState {
  token: string | null;
  role: UserRole | null;
  isAuthenticated: boolean;
}

/**
 * Auth store actions interface
 */
interface AuthActions {
  setToken: (token: string, role: UserRole) => void;
  logout: () => void;
}

/**
 * Zustand auth store for UrbanEats.
 * 
 * Features:
 * - Persisted to localStorage
 * - Simple token and role management
 * - Logout functionality
 * - Integrated with axios interceptors
 * - Auto-cleanup of expired tokens
 */
export const useAuthStore = create<AuthState & AuthActions>()(
  persist(
    (set, get) => ({
      // State
      token: null,
      role: null,
      isAuthenticated: false,

      // Actions
      setToken: (token: string, role: UserRole) => {
        set({
          token,
          role,
          isAuthenticated: true,
        });
      },

      logout: () => {
        set({
          token: null,
          role: null,
          isAuthenticated: false,
        });
      },
    }),
    {
      name: 'urbaneats-auth',
      // Add version for future migrations
      version: 1,
      // Validate and cleanup on hydration
      onRehydrateStorage: () => (state) => {
        if (state?.token) {
          // Check if token is expired (basic JWT check)
          try {
            const payload = JSON.parse(atob(state.token.split('.')[1]));
            const expiryTime = payload.exp * 1000; // Convert to milliseconds
            
            if (Date.now() >= expiryTime) {
              console.warn('⚠️ [Auth Store] Token expired on startup, clearing...');
              state.token = null;
              state.role = null;
              state.isAuthenticated = false;
            }
          } catch (error) {
            console.error('❌ [Auth Store] Invalid token format, clearing...');
            state.token = null;
            state.role = null;
            state.isAuthenticated = false;
          }
        }
      },
    }
  )
);

// Initialize auth hooks for axios interceptors
setAuthHooks(
  () => useAuthStore.getState().token,
  () => useAuthStore.getState().logout()
);
