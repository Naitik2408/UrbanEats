import apiClient from '../api/axios';
import { API_ENDPOINTS } from '../api/endpoints';
import type { ItemDetails, PlaceOrderRequest, Order } from '../types/cart.types';

/**
 * Order service for customer order operations
 */

/**
 * Fetch item details with variants and addons
 */
export const fetchItemDetails = async (itemId: number): Promise<ItemDetails> => {
  const response = await apiClient.get(API_ENDPOINTS.CUSTOMER_ITEMS.BY_ID(itemId));
  return response.data;
};

/**
 * Place a new order
 */
export const placeOrder = async (orderData: PlaceOrderRequest): Promise<Order> => {
  const response = await apiClient.post(API_ENDPOINTS.ORDERS.PLACE, orderData);
  return response.data;
};

/**
 * Fetch all orders for the authenticated user
 */
export const fetchOrders = async (): Promise<Order[]> => {
  const response = await apiClient.get(API_ENDPOINTS.ORDERS.LIST);
  // Backend returns Page<OrderResponse>, extract content array
  return response.data.content || [];
};

/**
 * Fetch a specific order by ID
 */
export const fetchOrderById = async (orderId: number): Promise<Order> => {
  const response = await apiClient.get(API_ENDPOINTS.ORDERS.BY_ID(orderId));
  return response.data;
};

/**
 * Cancel an order (only if within 1 minute)
 */
export const cancelOrder = async (orderId: number): Promise<void> => {
  await apiClient.post(API_ENDPOINTS.ORDERS.CANCEL(orderId));
};
