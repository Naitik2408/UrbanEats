import apiClient from '../api/axios';
import { API_ENDPOINTS } from '../api/endpoints';
import type {
  OtpRequest,
  OtpVerifyRequest,
  AdminLoginRequest,
  AuthResponse,
} from '../types/api.types';

/**
 * Auth service for UrbanEats.
 * Handles all authentication-related API calls.
 */

/**
 * Request OTP for customer login
 */
export const requestOtp = async (data: OtpRequest): Promise<{ otp: string }> => {
  const response = await apiClient.post(API_ENDPOINTS.AUTH.REQUEST_OTP, data);
  return response.data;
};

/**
 * Verify OTP and get authentication token
 */
export const verifyOtp = async (data: OtpVerifyRequest): Promise<AuthResponse> => {
  const response = await apiClient.post(API_ENDPOINTS.AUTH.VERIFY_OTP, data);
  return response.data;
};

/**
 * Admin login with username and password
 */
export const adminLogin = async (data: AdminLoginRequest): Promise<AuthResponse> => {
  const response = await apiClient.post(API_ENDPOINTS.AUTH.ADMIN_LOGIN, data);
  return response.data;
};
