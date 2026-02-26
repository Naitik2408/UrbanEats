import { apiClient } from './api.service';
import { type HealthResponse } from '@/types/api.types';

/**
 * Public API service for non-authenticated endpoints
 */
export const publicService = {
  /**
   * Check application health status
   */
  checkHealth: async (): Promise<HealthResponse> => {
    const response = await apiClient.get<HealthResponse>('/api/public/health');
    return response.data;
  },
};
