import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * Customer Store
 * 
 * Manages customer browsing state including:
 * - Selected city
 * - Selected restaurant
 * - Browsing history
 */

interface CustomerState {
  selectedCityId: string | null;
  selectedRestaurantId: string | null;
  setCity: (cityId: string) => void;
  setRestaurant: (restaurantId: string) => void;
  clear: () => void;
}

export const useCustomerStore = create<CustomerState>()(
  persist(
    (set) => ({
      selectedCityId: null,
      selectedRestaurantId: null,

      setCity: (cityId: string) =>
        set({ selectedCityId: cityId, selectedRestaurantId: null }),

      setRestaurant: (restaurantId: string) =>
        set({ selectedRestaurantId: restaurantId }),

      clear: () =>
        set({ selectedCityId: null, selectedRestaurantId: null }),
    }),
    {
      name: 'customer-storage',
    }
  )
);
