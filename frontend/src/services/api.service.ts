import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, AxiosError } from 'axios';
import { type ApiResponse, type ApiError } from '@/types/api.types';

/**
 * Axios API client configuration for UrbanEats.
 * Provides centralized API communication with interceptors for authentication and error handling.
 */
class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
      timeout: parseInt(import.meta.env.VITE_API_TIMEOUT || '30000'),
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  /**
   * Setup request and response interceptors
   */
  private setupInterceptors(): void {
    // Request interceptor - Add JWT token when available
    this.client.interceptors.request.use(
      (config) => {
        const token = this.getToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor - Handle errors globally
    this.client.interceptors.response.use(
      (response: AxiosResponse) => {
        return response;
      },
      (error: AxiosError<ApiError>) => {
        return this.handleError(error);
      }
    );
  }

  /**
   * Get JWT token from localStorage
   */
  private getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  /**
   * Handle API errors globally
   */
  private handleError(error: AxiosError<ApiError>): Promise<never> {
    if (error.response) {
      // Server responded with error status
      const apiError = error.response.data;
      console.error('API Error:', apiError);

      // Handle 401 Unauthorized - redirect to login
      if (error.response.status === 401) {
        localStorage.removeItem('auth_token');
        window.location.href = '/login';
      }

      return Promise.reject(apiError);
    } else if (error.request) {
      // Request made but no response received
      console.error('Network Error:', error.message);
      return Promise.reject({
        title: 'Network Error',
        detail: 'Unable to connect to the server. Please check your internet connection.',
        status: 0,
        timestamp: new Date().toISOString(),
      } as ApiError);
    } else {
      // Something else happened
      console.error('Request Error:', error.message);
      return Promise.reject({
        title: 'Request Error',
        detail: error.message,
        status: 0,
        timestamp: new Date().toISOString(),
      } as ApiError);
    }
  }

  /**
   * Generic GET request
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.get<T>(url, config);
    return {
      data: response.data,
      status: response.status,
    };
  }

  /**
   * Generic POST request
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.post<T>(url, data, config);
    return {
      data: response.data,
      status: response.status,
    };
  }

  /**
   * Generic PUT request
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.put<T>(url, data, config);
    return {
      data: response.data,
      status: response.status,
    };
  }

  /**
   * Generic PATCH request
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.patch<T>(url, data, config);
    return {
      data: response.data,
      status: response.status,
    };
  }

  /**
   * Generic DELETE request
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.client.delete<T>(url, config);
    return {
      data: response.data,
      status: response.status,
    };
  }
}

// Export singleton instance
export const apiClient = new ApiClient();
export default apiClient;
