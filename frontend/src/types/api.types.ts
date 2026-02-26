/**
 * API Response type wrapper
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface ApiResponse<T = any> {
  data: T;
  message?: string;
  status: number;
}

/**
 * API Error type
 */
export interface ApiError {
  title: string;
  detail: string;
  status: number;
  timestamp: string;
  errors?: Record<string, string>;
}

/**
 * Health check response type
 */
export interface HealthResponse {
  status: string;
  service: string;
}
