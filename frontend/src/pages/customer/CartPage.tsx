import { useNavigate } from 'react-router-dom';
import { ShoppingBag, ArrowLeft, Trash2 } from 'lucide-react';
import { useCartStore } from '../../store/cartStore';
import { Button } from '../../components/ui/Button';
import CartItemCard from '../../components/cart/CartItemCard';

/**
 * Cart Page
 * Displays cart items with quantity management and checkout
 */
export default function CartPage() {
  const navigate = useNavigate();
  const { items, updateQuantity, removeItem, clearCart, totalAmount } = useCartStore();

  const total = totalAmount();
  const isEmpty = items.length === 0;

  const handleCheckout = () => {
    navigate('/customer/checkout');
  };

  const handleClearCart = () => {
    if (window.confirm('Are you sure you want to clear your cart?')) {
      clearCart();
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <button
              onClick={() => navigate(-1)}
              className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
              <span className="font-medium">Back</span>
            </button>
            <h1 className="text-2xl font-bold text-gray-900">Your Cart</h1>
            {!isEmpty && (
              <button
                onClick={handleClearCart}
                className="flex items-center gap-2 text-red-500 hover:text-red-700 transition-colors"
              >
                <Trash2 className="w-5 h-5" />
                <span className="font-medium">Clear</span>
              </button>
            )}
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-6">
        {isEmpty ? (
          // Empty State
          <div className="bg-white rounded-xl shadow-sm p-12 text-center">
            <div className="inline-flex items-center justify-center w-24 h-24 bg-gray-100 rounded-full mb-6">
              <ShoppingBag className="w-12 h-12 text-gray-400" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Your cart is empty
            </h2>
            <p className="text-gray-600 mb-6">
              Add some delicious items to get started!
            </p>
            <Button
              onClick={() => navigate('/customer/cities')}
              className="bg-orange-500 hover:bg-orange-600"
            >
              Browse Restaurants
            </Button>
          </div>
        ) : (
          // Cart Items
          <div className="grid gap-6 lg:grid-cols-3">
            {/* Items List */}
            <div className="lg:col-span-2 space-y-4">
              <div className="bg-white rounded-xl shadow-sm p-4">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">
                  Items ({items.length})
                </h2>
                <div className="space-y-3">
                  {items.map((item) => (
                    <CartItemCard
                      key={`${item.itemId}-${item.variantId || 0}`}
                      item={item}
                      onUpdateQuantity={updateQuantity}
                      onRemove={removeItem}
                    />
                  ))}
                </div>
              </div>
            </div>

            {/* Order Summary - Sticky on desktop */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-xl shadow-sm p-6 sticky top-24">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">
                  Order Summary
                </h2>
                
                {/* Price Breakdown */}
                <div className="space-y-3 mb-6">
                  <div className="flex justify-between text-gray-600">
                    <span>Subtotal</span>
                    <span>₹{total.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-gray-600">
                    <span>Delivery Fee</span>
                    <span className="text-green-600 font-medium">FREE</span>
                  </div>
                  <div className="flex justify-between text-gray-600">
                    <span>Taxes & Fees</span>
                    <span>₹0.00</span>
                  </div>
                  
                  <div className="pt-3 border-t border-gray-200">
                    <div className="flex justify-between items-center">
                      <span className="text-lg font-bold text-gray-900">Total</span>
                      <span className="text-2xl font-bold text-orange-600">
                        ₹{total.toFixed(2)}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Checkout Button */}
                <Button
                  onClick={handleCheckout}
                  className="w-full bg-orange-500 hover:bg-orange-600 text-white font-semibold py-3"
                >
                  Proceed to Checkout
                </Button>

                {/* Info */}
                <p className="text-xs text-gray-500 text-center mt-4">
                  Free delivery on all orders
                </p>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Mobile Sticky Footer - Only show when cart has items */}
      {!isEmpty && (
        <div className="lg:hidden fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg p-4 z-20">
          <div className="flex items-center justify-between mb-3">
            <span className="font-semibold text-gray-900">Total</span>
            <span className="text-xl font-bold text-orange-600">
              ₹{total.toFixed(2)}
            </span>
          </div>
          <Button
            onClick={handleCheckout}
            className="w-full bg-orange-500 hover:bg-orange-600 text-white font-semibold py-3"
          >
            Proceed to Checkout
          </Button>
        </div>
      )}
    </div>
  );
}
