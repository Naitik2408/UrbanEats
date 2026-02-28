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
  role?: string;
  userId?: number;
  message?: string;
}

/**
 * Pagination types
 */
export interface PaginationParams {
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

/**
 * Customer browsing types
 */
export interface City {
  id: string;
  name: string;
  state: string;
  country: string;
}

export interface Restaurant {
  id: string;
  name: string;
  address: string;
  landmark: string;
  cityId: string;
  rating: number;
  isActive: boolean;
}

export interface Item {
  id: string;
  name: string;
  description: string;
  basePrice: number;
  restaurantId: string;
  categoryId: string;
  isAvailable: boolean;
  hasVariants: boolean;
  hasAddons: boolean;
}

/**
 * Search params
 */
export interface SearchParams extends PaginationParams {
  q: string;
}
