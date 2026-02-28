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
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
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
      // Clear auth state on 401
      clearAuth?.();
      
      // Redirect to login
      if (typeof window !== 'undefined') {
        window.location.href = '/auth/customer-login';
      }
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
