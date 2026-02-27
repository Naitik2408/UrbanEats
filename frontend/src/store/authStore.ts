import { create } from 'zustand';

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
 * - In-memory state (no localStorage yet)
 * - Simple token and role management
 * - Logout functionality
 * 
 * Note: localStorage integration will be added in auth flow implementation.
 */
export const useAuthStore = create<AuthState & AuthActions>((set) => ({
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
}));
