import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Package, Clock, CheckCircle, XCircle, Timer } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { fetchOrders, cancelOrder } from '../../services/order.service';
import { useToastStore } from '../../store/toastStore';
import { Button } from '../../components/ui/Button';
import { Loader } from '../../components/ui/Loader';
import type { Order } from '../../types/cart.types';

/**
 * Calculate remaining time in seconds for order cancellation (1 minute window)
 */
const getRemainingCancelTime = (createdAt: string): number => {
  const createdTime = new Date(createdAt).getTime();
  const currentTime = new Date().getTime();
  const elapsedSeconds = Math.floor((currentTime - createdTime) / 1000);
  const remainingSeconds = 60 - elapsedSeconds;
  return Math.max(0, remainingSeconds);
};

/**
 * Format seconds to MM:SS
 */
const formatTime = (seconds: number): string => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

/**
 * Order Card with Cancel Timer
 */
function OrderCard({ order }: { order: Order }) {
  const queryClient = useQueryClient();
  const addToast = useToastStore((state) => state.addToast);
  const [remainingTime, setRemainingTime] = useState(getRemainingCancelTime(order.createdAt));

  // Update timer every second
  useEffect(() => {
    if (remainingTime <= 0) return;

    const interval = setInterval(() => {
      const newTime = getRemainingCancelTime(order.createdAt);
      setRemainingTime(newTime);
      
      if (newTime <= 0) {
        clearInterval(interval);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [order.createdAt, remainingTime]);

  // Cancel order mutation
  const cancelMutation = useMutation({
    mutationFn: () => cancelOrder(order.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      addToast('Order cancelled successfully', 'success');
    },
    onError: (error: any) => {
      addToast(error.response?.data?.detail || 'Failed to cancel order', 'error');
    },
  });

  const canCancel = remainingTime > 0 && order.status !== 'CANCELLED';

  const getStatusIcon = () => {
    switch (order.status) {
      case 'PLACED':
        return <Clock className="w-5 h-5 text-blue-500" />;
      case 'CONFIRMED':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'CANCELLED':
        return <XCircle className="w-5 h-5 text-red-500" />;
      default:
        return <Package className="w-5 h-5 text-gray-500" />;
    }
  };

  const getStatusColor = () => {
    switch (order.status) {
      case 'PLACED':
        return 'bg-blue-100 text-blue-700';
      case 'CONFIRMED':
        return 'bg-green-100 text-green-700';
      case 'CANCELLED':
        return 'bg-red-100 text-red-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow">
      {/* Header */}
      <div className="flex items-start justify-between mb-4">
        <div>
          <div className="flex items-center gap-2 mb-2">
            {getStatusIcon()}
            <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor()}`}>
              {order.status}
            </span>
          </div>
          <p className="text-sm text-gray-600">
            Order #{order.id} • {new Date(order.createdAt).toLocaleString()}
          </p>
        </div>
        <div className="text-right">
          <p className="text-2xl font-bold text-gray-900">₹{order.totalAmount.toFixed(2)}</p>
        </div>
      </div>

      {/* Items */}
      <div className="border-t border-gray-200 pt-4 mb-4">
        <h3 className="font-semibold text-gray-900 mb-2">Items:</h3>
        <div className="space-y-2">
          {order.items.map((item) => (
            <div key={item.id} className="flex justify-between text-sm">
              <span className="text-gray-700">
                {item.itemName}
                {item.variantName && ` (${item.variantName})`} × {item.quantity}
              </span>
              <span className="font-medium text-gray-900">₹{item.price.toFixed(2)}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Cancel Button with Timer */}
      {canCancel && (
        <div className="border-t border-gray-200 pt-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-sm">
              <Timer className="w-4 h-4 text-orange-500" />
              <span className="text-gray-600">
                Cancel within: <span className="font-mono font-bold text-orange-600">{formatTime(remainingTime)}</span>
              </span>
            </div>
            <Button
              onClick={() => {
                if (window.confirm('Are you sure you want to cancel this order?')) {
                  cancelMutation.mutate();
                }
              }}
              disabled={cancelMutation.isPending}
              variant="outline"
              className="text-red-600 border-red-600 hover:bg-red-50"
            >
              {cancelMutation.isPending ? 'Cancelling...' : 'Cancel Order'}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}

/**
 * Orders Page
 * Display user's orders with cancel functionality
 */
export default function OrdersPage() {
  const navigate = useNavigate();

  // Fetch orders
  const { data: orders, isLoading, error } = useQuery({
    queryKey: ['orders'],
    queryFn: fetchOrders,
  });

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Loader />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="text-center">
          <p className="text-red-600 mb-4">Failed to load orders</p>
          <Button onClick={() => window.location.reload()}>Retry</Button>
        </div>
      </div>
    );
  }

  const isEmpty = !orders || orders.length === 0;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate(-1)}
              className="text-gray-600 hover:text-gray-900 transition-colors"
            >
              <ArrowLeft className="w-6 h-6" />
            </button>
            <h1 className="text-2xl font-bold text-gray-900">My Orders</h1>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-6">
        {isEmpty ? (
          // Empty State
          <div className="bg-white rounded-xl shadow-sm p-12 text-center">
            <div className="inline-flex items-center justify-center w-24 h-24 bg-gray-100 rounded-full mb-6">
              <Package className="w-12 h-12 text-gray-400" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              No orders yet
            </h2>
            <p className="text-gray-600 mb-6">
              Start ordering delicious food now!
            </p>
            <Button
              onClick={() => navigate('/customer/cities')}
              className="bg-orange-500 hover:bg-orange-600"
            >
              Browse Restaurants
            </Button>
          </div>
        ) : (
          // Orders List
          <div className="space-y-4">
            {orders.map((order) => (
              <OrderCard key={order.id} order={order} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
