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
    // Get token from auth store (will be implemented in auth flow)
    const token = localStorage.getItem('auth_token');
    
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
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_role');
      
      // Redirect to auth page (will be handled by router)
      if (typeof window !== 'undefined') {
        window.location.href = '/auth';
      }
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
