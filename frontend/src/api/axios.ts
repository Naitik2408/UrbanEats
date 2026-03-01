import axios, { type AxiosInstance, type AxiosError } from 'axios';

/**
 * Axios instance configuration for UrbanEats API.
 * 
 * Features:
 * - Base URL from environment variables
 * - 10-second timeout
 * - Request interceptor for JWT token attachment
 * - Response interceptor for 401 handling
 */

// Import will be used after store initialization
let getAuthToken: (() => string | null) | null = null;
let clearAuth: (() => void) | null = null;

/**
 * Set auth store hooks for interceptors
 * Called after store is initialized
 */
export const setAuthHooks = (
  getToken: () => string | null,
  onLogout: () => void
) => {
  getAuthToken = getToken;
  clearAuth = onLogout;
};

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000, // 10 seconds
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor - Attach JWT token if exists
 */
apiClient.interceptors.request.use(
  (config) => {
    // Get token from auth store
    const token = getAuthToken?.();
    
    if (import.meta.env.DEV) {
      console.log('🔐 [Axios] Request to:', config.url);
      console.log('🎫 [Axios] Token available:', !!token);
      if (token) {
        console.log('🔑 [Axios] Token (first 20 chars):', token.substring(0, 20) + '...');
      }
    }
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    } else if (import.meta.env.DEV) {
      console.log('⚠️ [Axios] No token available!');
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor - Handle 401 errors
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      console.warn('⚠️ [Axios] 401 Unauthorized - Clearing auth and redirecting to login');
      
      // Clear auth state on 401
      clearAuth?.();
      
      // Show user-friendly message
      if (typeof window !== 'undefined') {
        // Create a toast-like notification
        const message = 'Your session has expired. Please login again.';
        
        // Try to use toast if available, otherwise alert
        try {
          // Dynamically import and use toast store
          import('../store/toastStore').then((module) => {
            module.useToastStore.getState().addToast(message, 'warning', 4000);
          });
        } catch {
          // Fallback to console
          console.log('Session expired:', message);
        }
        
        // Redirect to login after a short delay
        setTimeout(() => {
          window.location.href = '/auth/customer-login';
        }, 1500);
      }
    }
    
    // Handle network errors
    if (error.code === 'ERR_NETWORK' || error.message === 'Network Error') {
      console.error('🌐 [Axios] Network error - backend might be down');
      
      if (typeof window !== 'undefined') {
        try {
          import('../store/toastStore').then((module) => {
            module.useToastStore.getState().addToast(
              'Cannot connect to server. Please check your connection.',
              'error',
              4000
            );
          });
        } catch {
          console.error('Network error: Cannot connect to server');
        }
      }
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
