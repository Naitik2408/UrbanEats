import { memo } from 'react';
import { Minus, Plus, Trash2 } from 'lucide-react';
import type { CartItem } from '../../store/cartStore';

interface CartItemCardProps {
  item: CartItem;
  onUpdateQuantity: (itemId: number, variantId: number | undefined, quantity: number) => void;
  onRemove: (itemId: number, variantId: number | undefined) => void;
}

/**
 * Cart Item Card Component
 * Memoized for performance
 */
const CartItemCard = memo(({ item, onUpdateQuantity, onRemove }: CartItemCardProps) => {
  return (
    <div className="flex gap-4 p-4 bg-white rounded-lg border border-gray-200 hover:shadow-md transition-shadow">
      {/* Item Details */}
      <div className="flex-1">
        <h3 className="font-semibold text-gray-900 mb-1">{item.name}</h3>
        
        {/* Variant */}
        {item.variantName && (
          <p className="text-sm text-gray-600 mb-1">
            <span className="font-medium">Variant:</span> {item.variantName}
          </p>
        )}
        
        {/* Addons */}
        {item.addonNames && item.addonNames.length > 0 && (
          <p className="text-sm text-gray-600 mb-2">
            <span className="font-medium">Add-ons:</span> {item.addonNames.join(', ')}
          </p>
        )}
        
        {/* Price per unit */}
        <p className="text-sm text-gray-500">
          ₹{(item.finalPrice / item.quantity).toFixed(2)} per item
        </p>
      </div>

      {/* Quantity Controls */}
      <div className="flex flex-col items-end justify-between">
        <div className="flex items-center gap-2 bg-gray-50 rounded-lg p-1">
          <button
            onClick={() => onUpdateQuantity(item.itemId, item.variantId, item.quantity - 1)}
            className="w-8 h-8 flex items-center justify-center rounded-md hover:bg-gray-200 transition-colors"
            disabled={item.quantity <= 1}
            aria-label="Decrease quantity"
          >
            <Minus className="w-4 h-4 text-gray-700" />
          </button>
          <span className="w-8 text-center font-semibold text-gray-900">
            {item.quantity}
          </span>
          <button
            onClick={() => onUpdateQuantity(item.itemId, item.variantId, item.quantity + 1)}
            className="w-8 h-8 flex items-center justify-center rounded-md hover:bg-gray-200 transition-colors"
            aria-label="Increase quantity"
          >
            <Plus className="w-4 h-4 text-gray-700" />
          </button>
        </div>

        {/* Price */}
        <div className="text-right">
          <p className="text-lg font-bold text-gray-900">
            ₹{item.finalPrice.toFixed(2)}
          </p>
        </div>
      </div>

      {/* Remove Button */}
      <button
        onClick={() => onRemove(item.itemId, item.variantId)}
        className="self-start text-red-500 hover:text-red-700 hover:bg-red-50 p-2 rounded-md transition-colors"
        aria-label="Remove item"
      >
        <Trash2 className="w-5 h-5" />
      </button>
    </div>
  );
});

CartItemCard.displayName = 'CartItemCard';

export default CartItemCard;
