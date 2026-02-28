import { apiClient } from './api.service';
import type {
  City,
  Restaurant,
  Item,
  PageResponse,
  PaginationParams,
  SearchParams,
} from '@/types/api.types';

/**
 * Customer API Service
 * 
 * Handles all customer-facing API calls for browsing cities, restaurants, and items.
 * Uses React Query for data fetching and caching.
 */

/**
 * Fetch paginated list of cities
 */
export const fetchCities = async (
  params: PaginationParams = {}
): Promise<PageResponse<City>> => {
  const { page = 0, size = 10 } = params;
  const response = await apiClient.get<PageResponse<City>>('/api/customer/cities', {
    params: { page, size },
  });
  return response.data;
};

/**
 * Search cities by name
 */
export const searchCities = async (
  params: SearchParams
): Promise<PageResponse<City>> => {
  const { q, page = 0, size = 10 } = params;
  const response = await apiClient.get<PageResponse<City>>('/api/customer/search/cities', {
    params: { q, page, size },
  });
  return response.data;
};

/**
 * Fetch paginated list of restaurants in a city
 */
export const fetchRestaurants = async (
  cityId: string,
  params: PaginationParams = {}
): Promise<PageResponse<Restaurant>> => {
  const { page = 0, size = 10 } = params;
  const response = await apiClient.get<PageResponse<Restaurant>>('/api/customer/restaurants', {
    params: { cityId, page, size },
  });
  return response.data;
};

/**
 * Search restaurants by name
 */
export const searchRestaurants = async (
  params: SearchParams
): Promise<PageResponse<Restaurant>> => {
  const { q, page = 0, size = 10 } = params;
  const response = await apiClient.get<PageResponse<Restaurant>>('/api/customer/search/restaurants', {
    params: { q, page, size },
  });
  return response.data;
};

/**
 * Fetch paginated list of items in a restaurant
 */
export const fetchItems = async (
  restaurantId: string,
  params: PaginationParams = {}
): Promise<PageResponse<Item>> => {
  const { page = 0, size = 10 } = params;
  const response = await apiClient.get<PageResponse<Item>>('/api/customer/items', {
    params: { restaurantId, page, size },
  });
  return response.data;
};

/**
 * Search items by name
 */
export const searchItems = async (
  params: SearchParams
): Promise<PageResponse<Item>> => {
  const { q, page = 0, size = 10 } = params;
  const response = await apiClient.get<PageResponse<Item>>('/api/customer/search/items', {
    params: { q, page, size },
  });
  return response.data;
};
