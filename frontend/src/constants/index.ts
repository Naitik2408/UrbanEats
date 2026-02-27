/**
 * Application-wide constants.
 * 
 * Centralized location for all constant values used across the app.
 */

/**
 * API Configuration
 */
export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  TIMEOUT: parseInt(import.meta.env.VITE_API_TIMEOUT || '10000', 10),
} as const;

/**
 * Cache Time Constants (in milliseconds)
 */
export const CACHE_TIME = {
  SHORT: 5 * 60 * 1000, // 5 minutes
  MEDIUM: 15 * 60 * 1000, // 15 minutes
  LONG: 30 * 60 * 1000, // 30 minutes
} as const;

/**
 * Pagination Constants
 */
export const PAGINATION = {
  DEFAULT_PAGE: 0,
  DEFAULT_SIZE: 10,
  MAX_SIZE: 100,
} as const;

/**
 * User Roles
 */
export const USER_ROLES = {
  ADMIN: 'ADMIN',
  CUSTOMER: 'CUSTOMER',
} as const;

/**
 * Order Status Values
 */
export const ORDER_STATUS = {
  PENDING: 'PENDING',
  CONFIRMED: 'CONFIRMED',
  PREPARING: 'PREPARING',
  OUT_FOR_DELIVERY: 'OUT_FOR_DELIVERY',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED',
} as const;

/**
 * Local Storage Keys
 */
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'urbanEats_authToken',
  USER_ROLE: 'urbanEats_userRole',
} as const;

/**
 * Routes
 */
export const ROUTES = {
  HOME: '/',
  AUTH: '/auth',
  ADMIN: '/admin',
  CUSTOMER: '/customer',
} as const;
