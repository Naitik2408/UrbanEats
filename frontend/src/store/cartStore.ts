import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * Cart Item structure with variant and addon support
 */
export interface CartItem {
  itemId: number;
  name: string;
  basePrice: number;
  quantity: number;
  variantId?: number;
  variantName?: string;
  variantPrice?: number;
  addonIds?: number[];
  addonNames?: string[];
  addonPrices?: number[];
  finalPrice: number;
}

/**
 * Cart Store State
 */
interface CartState {
  items: CartItem[];
  addItem: (item: CartItem) => void;
  updateQuantity: (itemId: number, variantId: number | undefined, quantity: number) => void;
  removeItem: (itemId: number, variantId: number | undefined) => void;
  clearCart: () => void;
  totalAmount: () => number;
  itemCount: () => number;
}



/**
 * Cart Store with Zustand
 * Persists to localStorage for user convenience
 */
export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],

      /**
       * Add item to cart
       * If same configuration exists, increase quantity
       */
      addItem: (newItem: CartItem) => {
        set((state) => {
          const existingIndex = state.items.findIndex(
            (item) =>
              item.itemId === newItem.itemId &&
              item.variantId === newItem.variantId &&
              JSON.stringify(item.addonIds?.sort()) === JSON.stringify(newItem.addonIds?.sort())
          );

          if (existingIndex !== -1) {
            // Item with same config exists, increase quantity
            const updatedItems = [...state.items];
            updatedItems[existingIndex] = {
              ...updatedItems[existingIndex],
              quantity: updatedItems[existingIndex].quantity + newItem.quantity,
              finalPrice:
                updatedItems[existingIndex].finalPrice + newItem.finalPrice,
            };
            return { items: updatedItems };
          } else {
            // New item configuration
            return { items: [...state.items, newItem] };
          }
        });
      },

      /**
       * Update quantity of cart item
       */
      updateQuantity: (itemId: number, variantId: number | undefined, quantity: number) => {
        set((state) => {
          if (quantity <= 0) {
            // Remove item if quantity is 0
            return {
              items: state.items.filter(
                (item) => !(item.itemId === itemId && item.variantId === variantId)
              ),
            };
          }

          const updatedItems = state.items.map((item) => {
            if (item.itemId === itemId && item.variantId === variantId) {
              const pricePerUnit = item.finalPrice / item.quantity;
              return {
                ...item,
                quantity,
                finalPrice: pricePerUnit * quantity,
              };
            }
            return item;
          });

          return { items: updatedItems };
        });
      },

      /**
       * Remove item from cart
       */
      removeItem: (itemId: number, variantId: number | undefined) => {
        set((state) => ({
          items: state.items.filter(
            (item) => !(item.itemId === itemId && item.variantId === variantId)
          ),
        }));
      },

      /**
       * Clear entire cart
       */
      clearCart: () => {
        set({ items: [] });
      },

      /**
       * Calculate total amount
       */
      totalAmount: () => {
        const state = get();
        return state.items.reduce((total, item) => total + item.finalPrice, 0);
      },

      /**
       * Get total item count
       */
      itemCount: () => {
        const state = get();
        return state.items.reduce((count, item) => count + item.quantity, 0);
      },
    }),
    {
      name: 'urbaneats-cart',
    }
  )
);
