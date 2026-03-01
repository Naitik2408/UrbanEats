/**
 * Item variant details
 */
export interface ItemVariant {
  id: number;
  name: string;
  price: number;
}

/**
 * Item addon details
 */
export interface ItemAddon {
  id: number;
  name: string;
  price: number;
}

/**
 * Full item details with variants and addons
 */
export interface ItemDetails {
  id: number;
  name: string;
  description: string;
  basePrice: number;
  restaurantId: number;
  categoryId: number;
  isAvailable: boolean;
  hasVariants: boolean;
  hasAddons: boolean;
  variants?: ItemVariant[];
  addons?: ItemAddon[];
}

/**
 * Order item structure for API
 */
export interface OrderItem {
  itemId: number;
  quantity: number;
  variantId?: number;
  addonIds?: number[];
}

/**
 * Order request payload
 */
export interface PlaceOrderRequest {
  items: OrderItem[];
}

/**
 * Order response from API
 */
export interface Order {
  id: number;
  userId: number;
  restaurantId: number;
  totalAmount: number;
  status: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItemDetail[];
}

/**
 * Order item detail in response
 */
export interface OrderItemDetail {
  id: number;
  itemId: number;
  itemName: string;
  quantity: number;
  price: number;
  variantId?: number;
  variantName?: string;
  addonIds?: number[];
  addonNames?: string[];
}
