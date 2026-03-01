import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { ArrowLeft, CreditCard, Wallet, Building2, CheckCircle } from 'lucide-react';
import { useCartStore } from '../../store/cartStore';
import { useToastStore } from '../../store/toastStore';
import { placeOrder } from '../../services/order.service';
import { Button } from '../../components/ui/Button';
import { Loader } from '../../components/ui/Loader';
import type { OrderItem } from '../../types/cart.types';

/**
 * Checkout Page
 * Payment method selection and order placement
 */
export default function CheckoutPage() {
  const navigate = useNavigate();
  const { items, clearCart, totalAmount } = useCartStore();
  const addToast = useToastStore((state) => state.addToast);
  const [paymentMethod, setPaymentMethod] = useState<'CARD' | 'UPI' | 'CASH'>('CARD');
  const [showSuccess, setShowSuccess] = useState(false);

  const total = totalAmount();

  // Place order mutation
  const placeOrderMutation = useMutation({
    mutationFn: placeOrder,
    onSuccess: () => {
      // Clear cart
      clearCart();
      
      // Show success state
      setShowSuccess(true);
      
      // Navigate to orders page after 2 seconds
      setTimeout(() => {
        navigate('/customer/orders', { replace: true });
      }, 2000);
    },
    onError: (error: any) => {
      addToast(error.response?.data?.detail || 'Failed to place order. Please try again.', 'error');
    },
  });

  const handlePlaceOrder = () => {
    if (items.length === 0) {
      addToast('Your cart is empty', 'warning');
      return;
    }

    // Convert cart items to order items format
    const orderItems: OrderItem[] = items.map((item) => ({
      itemId: item.itemId,
      quantity: item.quantity,
      variantId: item.variantId,
      addonIds: item.addonIds,
    }));

    placeOrderMutation.mutate({ items: orderItems });
  };

  if (items.length === 0) {
    navigate('/customer/cart', { replace: true });
    return null;
  }

  // Success Screen
  if (showSuccess) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full text-center animate-in zoom-in-95 duration-300">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-green-100 rounded-full mb-6">
            <CheckCircle className="w-12 h-12 text-green-600" />
          </div>
          <h2 className="text-3xl font-bold text-gray-900 mb-2">Order Placed!</h2>
          <p className="text-gray-600 mb-4">
            Your order has been successfully placed.
          </p>
          <p className="text-sm text-gray-500">
            Redirecting to orders page...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 pb-20 lg:pb-6">
      {/* Header */}
      <div className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/customer/cart')}
              className="text-gray-600 hover:text-gray-900 transition-colors"
              disabled={placeOrderMutation.isPending}
            >
              <ArrowLeft className="w-6 h-6" />
            </button>
            <h1 className="text-2xl font-bold text-gray-900">Checkout</h1>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-6">
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Payment Method Selection */}
          <div className="lg:col-span-2 space-y-6">
            {/* Order Items Summary */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                Order Summary
              </h2>
              <div className="space-y-2">
                {items.map((item) => (
                  <div
                    key={`${item.itemId}-${item.variantId || 0}`}
                    className="flex justify-between text-sm"
                  >
                    <span className="text-gray-700">
                      {item.name} {item.variantName && `(${item.variantName})`} × {item.quantity}
                    </span>
                    <span className="font-medium text-gray-900">
                      ₹{item.finalPrice.toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Payment Method */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                Payment Method
              </h2>
              <div className="space-y-3">
                {/* Card */}
                <label
                  className={`flex items-center gap-4 p-4 border-2 rounded-lg cursor-pointer transition-all ${
                    paymentMethod === 'CARD'
                      ? 'border-orange-500 bg-orange-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    value="CARD"
                    checked={paymentMethod === 'CARD'}
                    onChange={(e) => setPaymentMethod(e.target.value as 'CARD')}
                    className="w-4 h-4 text-orange-500 focus:ring-orange-500"
                  />
                  <CreditCard className="w-6 h-6 text-gray-700" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">Credit / Debit Card</p>
                    <p className="text-sm text-gray-500">Pay securely with your card</p>
                  </div>
                </label>

                {/* UPI */}
                <label
                  className={`flex items-center gap-4 p-4 border-2 rounded-lg cursor-pointer transition-all ${
                    paymentMethod === 'UPI'
                      ? 'border-orange-500 bg-orange-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    value="UPI"
                    checked={paymentMethod === 'UPI'}
                    onChange={(e) => setPaymentMethod(e.target.value as 'UPI')}
                    className="w-4 h-4 text-orange-500 focus:ring-orange-500"
                  />
                  <Wallet className="w-6 h-6 text-gray-700" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">UPI</p>
                    <p className="text-sm text-gray-500">Pay via Google Pay, PhonePe, etc.</p>
                  </div>
                </label>

                {/* Cash */}
                <label
                  className={`flex items-center gap-4 p-4 border-2 rounded-lg cursor-pointer transition-all ${
                    paymentMethod === 'CASH'
                      ? 'border-orange-500 bg-orange-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    value="CASH"
                    checked={paymentMethod === 'CASH'}
                    onChange={(e) => setPaymentMethod(e.target.value as 'CASH')}
                    className="w-4 h-4 text-orange-500 focus:ring-orange-500"
                  />
                  <Building2 className="w-6 h-6 text-gray-700" />
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">Cash on Delivery</p>
                    <p className="text-sm text-gray-500">Pay when you receive</p>
                  </div>
                </label>
              </div>
            </div>
          </div>

          {/* Order Total - Sticky on desktop */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm p-6 sticky top-24">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                Payment Details
              </h2>
              
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
                  <span>Taxes</span>
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

              <Button
                onClick={handlePlaceOrder}
                disabled={placeOrderMutation.isPending}
                className="w-full bg-orange-500 hover:bg-orange-600 text-white font-semibold py-3"
              >
                {placeOrderMutation.isPending ? (
                  <span className="flex items-center justify-center gap-2">
                    <Loader size="sm" />
                    Placing Order...
                  </span>
                ) : (
                  'Place Order'
                )}
              </Button>

              <p className="text-xs text-gray-500 text-center mt-4">
                By placing this order, you agree to our terms & conditions
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Mobile Sticky Footer */}
      <div className="lg:hidden fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg p-4 z-20">
        <div className="flex items-center justify-between mb-3">
          <span className="font-semibold text-gray-900">Total</span>
          <span className="text-xl font-bold text-orange-600">
            ₹{total.toFixed(2)}
          </span>
        </div>
        <Button
          onClick={handlePlaceOrder}
          disabled={placeOrderMutation.isPending}
          className="w-full bg-orange-500 hover:bg-orange-600 text-white font-semibold py-3"
        >
          {placeOrderMutation.isPending ? (
            <span className="flex items-center justify-center gap-2">
              <Loader size="sm" />
              Placing Order...
            </span>
          ) : (
            'Place Order'
          )}
        </Button>
      </div>
    </div>
  );
}
