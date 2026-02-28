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

/**
 * Authentication types
 */
export interface OtpRequest {
  phone: string;
}

export interface OtpVerifyRequest {
  identifier: string;
  otp: string;
  firebaseToken?: string; // Optional: Firebase ID token for verification
}

export interface AdminLoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}
