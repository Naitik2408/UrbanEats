import React, { useState, useEffect, memo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Package, Clock, CheckCircle, XCircle, Timer, ChevronDown, ChevronUp } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { fetchOrders, cancelOrder } from '../../services/order.service';
import { useToastStore } from '../../store/toastStore';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Skeleton } from '../../components/ui/Skeleton';
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
 * Format date and time
 */
const formatDateTime = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

/**
 * Get status badge variant
 */
const getStatusVariant = (status: string): 'success' | 'warning' | 'error' => {
  switch (status.toUpperCase()) {
    case 'COMPLETED':
      return 'success';
    case 'PLACED':
    case 'CONFIRMED':
      return 'warning';
    case 'CANCELLED':
      return 'error';
    default:
      return 'warning';
  }
};

/**
 * Order Card with Cancel Timer
 */
const OrderCard = memo(({ order }: { order: Order }) => {
  const queryClient = useQueryClient();
  const addToast = useToastStore((state) => state.addToast);
  const [isExpanded, setIsExpanded] = useState(false);
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

  const canCancel = order.status === 'PLACED' && remainingTime > 0;

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
      {/* Card Header */}
      <div className="p-6">
        <div className="flex items-start justify-between mb-4">
          <div className="flex-1">
            <div className="flex items-center gap-3 mb-2">
              <span className="text-sm font-medium text-gray-500">Order #{order.id}</span>
              <Badge variant={getStatusVariant(order.status)}>
                {order.status}
              </Badge>
            </div>
            <p className="text-sm text-gray-600 flex items-center gap-1">
              <Clock className="w-4 h-4" />
              {formatDateTime(order.createdAt)}
            </p>
          </div>
          <div className="text-right">
            <p className="text-xs text-gray-500 mb-1">Total Amount</p>
            <p className="text-2xl font-bold text-gray-900">₹{order.totalAmount.toFixed(2)}</p>
          </div>
        </div>

        {/* Quick Item Summary */}
        <div className="mb-4">
          <p className="text-sm text-gray-600">
            {order.items.length} {order.items.length === 1 ? 'item' : 'items'}
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-between gap-4">
          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="flex items-center gap-2 text-sm text-blue-600 hover:text-blue-700 font-medium transition-colors"
          >
            {isExpanded ? (
              <>
                <ChevronUp className="w-4 h-4" />
                Hide Details
              </>
            ) : (
              <>
                <ChevronDown className="w-4 h-4" />
                View Details
              </>
            )}
          </button>

          {canCancel && (
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 text-sm">
                <Timer className="w-4 h-4 text-orange-500" />
                <span className="font-mono font-bold text-orange-600">{formatTime(remainingTime)}</span>
              </div>
              <Button
                onClick={() => {
                  if (window.confirm('Are you sure you want to cancel this order?')) {
                    cancelMutation.mutate();
                  }
                }}
                disabled={cancelMutation.isPending}
                variant="outline"
                size="sm"
                className="text-red-600 border-red-600 hover:bg-red-50"
              >
                {cancelMutation.isPending ? 'Cancelling...' : 'Cancel Order'}
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* Expandable Details Section */}
      {isExpanded && (
        <div className="border-t border-gray-200 bg-gray-50 p-6 animate-in slide-in-from-top duration-200">
          <h3 className="font-semibold text-gray-900 mb-4">Order Items</h3>
          <div className="space-y-3">
            {order.items.map((item) => (
              <div
                key={item.id}
                className="bg-white rounded-lg p-4 border border-gray-200"
              >
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <h4 className="font-medium text-gray-900">{item.itemName}</h4>
                    
                    {/* Variant */}
                    {item.variantName && (
                      <p className="text-sm text-gray-600 mt-1">
                        <span className="font-medium">Variant:</span> {item.variantName}
                      </p>
                    )}
                    
                    {/* Addons */}
                    {item.addonNames && item.addonNames.length > 0 && (
                      <p className="text-sm text-gray-600 mt-1">
                        <span className="font-medium">Add-ons:</span> {item.addonNames.join(', ')}
                      </p>
                    )}
                    
                    <p className="text-sm text-gray-500 mt-1">Quantity: {item.quantity}</p>
                  </div>
                  
                  <div className="text-right ml-4">
                    <p className="font-semibold text-gray-900">₹{item.price.toFixed(2)}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Price Breakdown */}
          <div className="mt-6 pt-4 border-t border-gray-200">
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Subtotal</span>
                <span className="text-gray-900">₹{order.totalAmount.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-base font-bold pt-2 border-t border-gray-200">
                <span className="text-gray-900">Total</span>
                <span className="text-gray-900">₹{order.totalAmount.toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
});

OrderCard.displayName = 'OrderCard';

/**
 * Loading Skeleton for Order Cards
 */
const OrderCardSkeleton = () => (
  <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <div className="flex items-start justify-between mb-4">
      <div className="flex-1">
        <Skeleton className="h-5 w-32 mb-2" />
        <Skeleton className="h-4 w-48" />
      </div>
      <Skeleton className="h-8 w-24" />
    </div>
    <Skeleton className="h-4 w-20 mb-4" />
    <div className="flex items-center justify-between">
      <Skeleton className="h-9 w-32" />
    </div>
  </div>
);

/**
 * Orders Page - Display user's orders with pagination and cancellation
 */
export default function OrdersPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const pageSize = 10;

  // Fetch orders with pagination
  const { data, isLoading, error } = useQuery({
    queryKey: ['orders', page],
    queryFn: () => fetchOrders(page, pageSize),
    staleTime: 30000, // 30 seconds
  });

  const orders = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const isFirstPage = page === 0;
  const isLastPage = data?.last || false;

  const handleNextPage = () => {
    if (!isLastPage) {
      setPage((prev) => prev + 1);
    }
  };

  const handlePrevPage = () => {
    if (!isFirstPage) {
      setPage((prev) => prev - 1);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Sticky Header */}
      <div className="bg-white border-b sticky top-0 z-10 shadow-sm">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate(-1)}
              className="text-gray-600 hover:text-gray-900 transition-colors"
              aria-label="Go back"
            >
              <ArrowLeft className="w-6 h-6" />
            </button>
            <h1 className="text-2xl font-bold text-gray-900">My Orders</h1>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Error State */}
        {error && (
          <div className="bg-white rounded-lg shadow-sm p-8 text-center">
            <XCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-gray-900 mb-2">Failed to load orders</h2>
            <p className="text-gray-600 mb-4">Please try again</p>
            <Button onClick={() => window.location.reload()}>Retry</Button>
          </div>
        )}

        {/* Loading Skeleton */}
        {isLoading && (
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <OrderCardSkeleton key={i} />
            ))}
          </div>
        )}

        {/* Empty State */}
        {!isLoading && !error && orders.length === 0 && (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <div className="inline-flex items-center justify-center w-24 h-24 bg-gray-100 rounded-full mb-6">
              <Package className="w-12 h-12 text-gray-400" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">No orders yet</h2>
            <p className="text-gray-600 mb-6">Start ordering delicious food now!</p>
            <Button
              onClick={() => navigate('/customer/cities')}
              className="bg-orange-500 hover:bg-orange-600"
            >
              Browse Restaurants
            </Button>
          </div>
        )}

        {/* Orders List */}
        {!isLoading && !error && orders.length > 0 && (
          <>
            <div className="space-y-4 mb-8">
              {orders.map((order) => (
                <OrderCard key={order.id} order={order} />
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                <div className="flex items-center justify-between">
                  <div className="text-sm text-gray-600">
                    Page {page + 1} of {totalPages}
                  </div>
                  <div className="flex gap-2">
                    <Button
                      onClick={handlePrevPage}
                      disabled={isFirstPage}
                      variant="outline"
                      size="sm"
                    >
                      Previous
                    </Button>
                    <Button
                      onClick={handleNextPage}
                      disabled={isLastPage}
                      variant="outline"
                      size="sm"
                    >
                      Next
                    </Button>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
