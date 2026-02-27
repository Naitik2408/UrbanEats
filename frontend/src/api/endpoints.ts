/**
 * API endpoints for UrbanEats backend.
 * Centralized endpoint definitions for maintainability.
 */

export const API_ENDPOINTS = {
  // Authentication
  AUTH: {
    ADMIN_LOGIN: '/api/auth/login',
    REQUEST_OTP: '/api/auth/request-otp',
    VERIFY_OTP: '/api/auth/verify-otp',
  },

  // Admin - Cities
  ADMIN_CITIES: {
    BASE: '/api/admin/cities',
    BY_ID: (id: number) => `/api/admin/cities/${id}`,
  },

  // Admin - Restaurants
  ADMIN_RESTAURANTS: {
    BASE: '/api/admin/restaurants',
    BY_ID: (id: number) => `/api/admin/restaurants/${id}`,
  },

  // Admin - Items
  ADMIN_ITEMS: {
    BASE: '/api/admin/items',
    BY_ID: (id: number) => `/api/admin/items/${id}`,
  },

  // Customer - Cities
  CUSTOMER_CITIES: {
    BASE: '/api/customer/cities',
  },

  // Customer - Restaurants
  CUSTOMER_RESTAURANTS: {
    BY_CITY: (cityId: number) => `/api/customer/restaurants/city/${cityId}`,
    BY_ID: (id: number) => `/api/customer/restaurants/${id}`,
  },

  // Customer - Items
  CUSTOMER_ITEMS: {
    BY_RESTAURANT: (restaurantId: number) => `/api/customer/items/restaurant/${restaurantId}`,
    BY_ID: (id: number) => `/api/customer/items/${id}`,
  },

  // Customer - Cart
  CART: {
    BASE: '/api/customer/cart',
    UPDATE: '/api/customer/cart/update',
    REMOVE: (itemId: number) => `/api/customer/cart/${itemId}`,
    CLEAR: '/api/customer/cart/clear',
  },

  // Customer - Orders
  ORDERS: {
    BASE: '/api/customer/orders',
    BY_ID: (id: number) => `/api/customer/orders/${id}`,
    CANCEL: (id: number) => `/api/customer/orders/${id}/cancel`,
  },

  // Search
  SEARCH: {
    ITEMS: '/api/customer/search/items',
    RESTAURANTS: '/api/customer/search/restaurants',
    CITIES: '/api/customer/search/cities',
  },

  // Health
  HEALTH: '/actuator/health',
} as const;
