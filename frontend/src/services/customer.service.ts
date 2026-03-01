import apiClient from '../api/axios';
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
 * Fetch all cities
 * Note: Backend returns full list (no pagination) as city list is typically small
 */
export const fetchCities = async (
  params: PaginationParams = {}
): Promise<City[]> => {
  const response = await apiClient.get('/api/customer/cities');
  return response.data;
};

/**
 * Search cities by name
 */
export const searchCities = async (
  params: SearchParams
): Promise<PageResponse<City>> => {
  const { q, page = 0, size = 10 } = params;
  const response = await apiClient.get('/api/customer/search/cities', {
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
  const response = await apiClient.get('/api/customer/restaurants', {
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
  const response = await apiClient.get('/api/customer/search/restaurants', {
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
  const response = await apiClient.get('/api/customer/items', {
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
  const response = await apiClient.get('/api/customer/search/items', {
    params: { q, page, size },
  });
  return response.data;
};
