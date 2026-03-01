import { useState, useMemo } from 'react';
import { X } from 'lucide-react';
import { Button } from '../ui/Button';
import type { ItemDetails, ItemVariant, ItemAddon } from '../../types/cart.types';
import type { CartItem } from '../../store/cartStore';

interface AddToCartModalProps {
  item: ItemDetails;
  isOpen: boolean;
  onClose: () => void;
  onAddToCart: (cartItem: CartItem) => void;
}

/**
 * Add to Cart Modal
 * Handles variant and addon selection before adding to cart
 */
export default function AddToCartModal({
  item,
  isOpen,
  onClose,
  onAddToCart,
}: AddToCartModalProps) {
  const [selectedVariant, setSelectedVariant] = useState<ItemVariant | null>(null);
  const [selectedAddons, setSelectedAddons] = useState<ItemAddon[]>([]);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState('');

  // Calculate total price dynamically
  const totalPrice = useMemo(() => {
    let price = item.basePrice;
    
    // Add variant price if selected
    if (selectedVariant) {
      price = selectedVariant.price;
    }
    
    // Add addon prices
    selectedAddons.forEach((addon) => {
      price += addon.price;
    });
    
    return price * quantity;
  }, [item.basePrice, selectedVariant, selectedAddons, quantity]);

  // Handle addon toggle
  const toggleAddon = (addon: ItemAddon) => {
    setError('');
    setSelectedAddons((prev) => {
      const exists = prev.find((a) => a.id === addon.id);
      if (exists) {
        return prev.filter((a) => a.id !== addon.id);
      } else {
        return [...prev, addon];
      }
    });
  };

  // Handle add to cart
  const handleAddToCart = () => {
    // Validate variant selection if required
    if (item.hasVariants && !selectedVariant) {
      setError('Please select a variant');
      return;
    }

    // Create cart item
    const cartItem: CartItem = {
      itemId: item.id,
      name: item.name,
      basePrice: item.basePrice,
      quantity,
      variantId: selectedVariant?.id,
      variantName: selectedVariant?.name,
      variantPrice: selectedVariant?.price,
      addonIds: selectedAddons.map((a) => a.id),
      addonNames: selectedAddons.map((a) => a.name),
      addonPrices: selectedAddons.map((a) => a.price),
      finalPrice: totalPrice,
    };

    onAddToCart(cartItem);
    handleClose();
  };

  // Handle close and reset
  const handleClose = () => {
    setSelectedVariant(null);
    setSelectedAddons([]);
    setQuantity(1);
    setError('');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-200">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/50"
        onClick={handleClose}
      />

      {/* Modal */}
      <div className="relative bg-white rounded-xl shadow-2xl max-w-lg w-full max-h-[90vh] overflow-hidden animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-start justify-between p-6 border-b">
          <div className="flex-1 pr-4">
            <h2 className="text-2xl font-bold text-gray-900">{item.name}</h2>
            {item.description && (
              <p className="text-sm text-gray-600 mt-1">{item.description}</p>
            )}
          </div>
          <button
            onClick={handleClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
            aria-label="Close"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto max-h-[60vh]">
          {/* Variants */}
          {item.hasVariants && item.variants && item.variants.length > 0 && (
            <div className="mb-6">
              <label className="block text-sm font-semibold text-gray-700 mb-3">
                Select Variant <span className="text-red-500">*</span>
              </label>
              <div className="space-y-2">
                {item.variants.map((variant) => (
                  <label
                    key={variant.id}
                    className={`flex items-center justify-between p-4 border-2 rounded-lg cursor-pointer transition-all ${
                      selectedVariant?.id === variant.id
                        ? 'border-orange-500 bg-orange-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <div className="flex items-center">
                      <input
                        type="radio"
                        name="variant"
                        checked={selectedVariant?.id === variant.id}
                        onChange={() => {
                          setSelectedVariant(variant);
                          setError('');
                        }}
                        className="w-4 h-4 text-orange-500 focus:ring-orange-500"
                      />
                      <span className="ml-3 font-medium text-gray-900">
                        {variant.name}
                      </span>
                    </div>
                    <span className="font-semibold text-gray-900">
                      ₹{variant.price.toFixed(2)}
                    </span>
                  </label>
                ))}
              </div>
            </div>
          )}

          {/* Addons */}
          {item.hasAddons && item.addons && item.addons.length > 0 && (
            <div className="mb-6">
              <label className="block text-sm font-semibold text-gray-700 mb-3">
                Add-ons (Optional)
              </label>
              <div className="space-y-2">
                {item.addons.map((addon) => (
                  <label
                    key={addon.id}
                    className={`flex items-center justify-between p-4 border-2 rounded-lg cursor-pointer transition-all ${
                      selectedAddons.find((a) => a.id === addon.id)
                        ? 'border-orange-500 bg-orange-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <div className="flex items-center">
                      <input
                        type="checkbox"
                        checked={!!selectedAddons.find((a) => a.id === addon.id)}
                        onChange={() => toggleAddon(addon)}
                        className="w-4 h-4 text-orange-500 rounded focus:ring-orange-500"
                      />
                      <span className="ml-3 font-medium text-gray-900">
                        {addon.name}
                      </span>
                    </div>
                    <span className="font-semibold text-gray-900">
                      +₹{addon.price.toFixed(2)}
                    </span>
                  </label>
                ))}
              </div>
            </div>
          )}

          {/* Quantity */}
          <div className="mb-6">
            <label className="block text-sm font-semibold text-gray-700 mb-3">
              Quantity
            </label>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => setQuantity(Math.max(1, quantity - 1))}
                className="w-10 h-10 flex items-center justify-center rounded-full border-2 border-gray-300 hover:border-orange-500 hover:text-orange-500 transition-colors"
                disabled={quantity <= 1}
              >
                <span className="text-xl font-semibold">−</span>
              </button>
              <span className="text-xl font-bold text-gray-900 w-12 text-center">
                {quantity}
              </span>
              <button
                onClick={() => setQuantity(quantity + 1)}
                className="w-10 h-10 flex items-center justify-center rounded-full border-2 border-gray-300 hover:border-orange-500 hover:text-orange-500 transition-colors"
              >
                <span className="text-xl font-semibold">+</span>
              </button>
            </div>
          </div>

          {/* Error */}
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-sm text-red-600">{error}</p>
            </div>
          )}

          {/* Price Breakdown */}
          <div className="bg-gray-50 rounded-lg p-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Base Price:</span>
              <span className="text-gray-900">₹{item.basePrice.toFixed(2)}</span>
            </div>
            {selectedVariant && (
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Variant ({selectedVariant.name}):</span>
                <span className="text-gray-900">₹{selectedVariant.price.toFixed(2)}</span>
              </div>
            )}
            {selectedAddons.map((addon) => (
              <div key={addon.id} className="flex justify-between text-sm">
                <span className="text-gray-600">+ {addon.name}:</span>
                <span className="text-gray-900">₹{addon.price.toFixed(2)}</span>
              </div>
            ))}
            <div className="flex justify-between text-sm">
              <span className="text-gray-600">Quantity:</span>
              <span className="text-gray-900">× {quantity}</span>
            </div>
            <div className="pt-2 border-t border-gray-300 flex justify-between">
              <span className="font-bold text-gray-900">Total:</span>
              <span className="font-bold text-xl text-orange-600">
                ₹{totalPrice.toFixed(2)}
              </span>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex gap-3 p-6 border-t bg-gray-50">
          <Button
            variant="outline"
            onClick={handleClose}
            className="flex-1"
          >
            Cancel
          </Button>
          <Button
            onClick={handleAddToCart}
            className="flex-1 bg-orange-500 hover:bg-orange-600"
          >
            Add to Cart
          </Button>
        </div>
      </div>
    </div>
  );
}
