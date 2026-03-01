/**
 * TypeScript type definitions for UrbanEats frontend.
 * Matches backend API contracts.
 */

// Re-export cart types
export * from './cart.types';

// ============================================
// Authentication Types
// ============================================

export type UserRole = 'ADMIN' | 'CUSTOMER';

export interface AdminLoginRequest {
  username: string;
  password: string;
}

export interface OtpRequest {
  phone: string;
}

export interface OtpVerifyRequest {
  identifier: string;
  otp: string;
}

export interface AuthResponse {
  token: string;
  role: UserRole;
}

// ============================================
// City Types
// ============================================

export interface City {
  id: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CityRequest {
  name: string;
}

// ============================================
// Restaurant Types
// ============================================

export interface Restaurant {
  id: number;
  name: string;
  address: string;
  landmark?: string;
  rating?: number;
  cityId: number;
  cityName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface RestaurantRequest {
  name: string;
  address: string;
  landmark?: string;
  rating?: number;
  cityId: number;
}

// ============================================
// Item Types
// ============================================

export interface ItemVariant {
  id: number;
  name: string;
  price: number;
}

export interface ItemAddon {
  id: number;
  name: string;
  price: number;
}

export interface Item {
  id: number;
  name: string;
  basePrice: number;
  hasVariants: boolean;
  hasAddons: boolean;
  restaurantId: number;
  restaurantName?: string;
  variants?: ItemVariant[];
  addons?: ItemAddon[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ItemRequest {
  name: string;
  basePrice: number;
  hasVariants: boolean;
  hasAddons: boolean;
  restaurantId: number;
  variants?: Array<{ name: string; price: number }>;
  addons?: Array<{ name: string; price: number }>;
}

// ============================================
// Cart Types
// ============================================

export interface CartItemAddon {
  id: number;
  addonName: string;
  addonPrice: number;
}

export interface CartItem {
  id: number;
  itemId: number;
  itemName: string;
  variantId?: number;
  variantName?: string;
  quantity: number;
  totalPrice: number;
  addons: CartItemAddon[];
}

export interface Cart {
  id: number;
  items: CartItem[];
  totalAmount: number;
}

export interface AddToCartRequest {
  itemId: number;
  variantId?: number;
  quantity: number;
  addonIds?: number[];
}

export interface UpdateCartRequest {
  cartItemId: number;
  quantity: number;
}

// ============================================
// Order Types
// ============================================

export type OrderStatus = 'PLACED' | 'CANCELLED';

export interface OrderItemAddon {
  addonName: string;
  addonPrice: number;
}

export interface OrderItem {
  orderItemId: number;
  itemName: string;
  basePrice: number;
  variantName?: string;
  variantPrice?: number;
  quantity: number;
  subtotal: number;
  addons: OrderItemAddon[];
}

export interface Order {
  orderId: number;
  restaurantName: string;
  totalAmount: number;
  status: OrderStatus;
  createdAt: string;
  canBeCancelled: boolean;
  items?: OrderItem[];
}

// ============================================
// Pagination Types
// ============================================

export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ============================================
// API Response Types
// ============================================

export interface ApiError {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  timestamp: string;
}

// ============================================
// Search Types
// ============================================

export interface SearchParams {
  q: string;
  page?: number;
  size?: number;
}
