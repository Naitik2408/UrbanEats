import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { fetchItems, searchItems } from '@/services/customer.service';
import { fetchItemDetails } from '@/services/order.service';
import { useCartStore } from '@/store/cartStore';
import { useToastStore } from '@/store/toastStore';
import { Card } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Skeleton } from '@/components/ui/Skeleton';
import { useDebounce } from '@/hooks/useDebounce';
import AddToCartModal from '@/components/cart/AddToCartModal';
import type { CartItem } from '@/store/cartStore';

/**
 * Item List Page
 * 
 * Displays menu items from a selected restaurant.
 * Features:
 * - Item browsing with details
 * - Search functionality
 * - Price display
 * - Variant and addon indicators
 * - Pagination
 */
export default function ItemListPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const restaurantId = searchParams.get('restaurantId');
  
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const debouncedSearch = useDebounce(searchQuery, 300);

  // Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Cart store
  const addItem = useCartStore((state) => state.addItem);
  const addToast = useToastStore((state) => state.addToast);

  // Redirect if no restaurant selected
  useEffect(() => {
    if (!restaurantId) {
      navigate('/customer/restaurants');
    }
  }, [restaurantId, navigate]);

  // Fetch items or search results
  const { data, isLoading, error } = useQuery({
    queryKey: ['items', restaurantId, page, debouncedSearch],
    queryFn: () => {
      if (!restaurantId) throw new Error('No restaurant selected');
      
      return debouncedSearch
        ? searchItems({ q: debouncedSearch, page, size: 12 })
        : fetchItems(restaurantId, { page, size: 12 });
    },
    enabled: !!restaurantId,
  });

  // Fetch item details for modal
  const itemDetailsMutation = useMutation({
    mutationFn: (itemId: number) => fetchItemDetails(itemId),
    onSuccess: () => {
      setIsModalOpen(true);
    },
    onError: (error) => {
      console.error('Failed to fetch item details:', error);
      addToast('Failed to load item details. Please try again.', 'error');
    },
  });

  const handleItemClick = (itemId: number) => {
    itemDetailsMutation.mutate(itemId);
  };

  const handleAddToCart = (cartItem: CartItem) => {
    addItem(cartItem);
    setIsModalOpen(false);
    addToast('Item added to cart!', 'success');
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
  };

  const handleBackToRestaurants = () => {
    navigate('/customer/restaurants');
  };

  const handleNextPage = () => {
    if (data && !data.last) {
      setPage((prev) => prev + 1);
    }
  };

  const handlePrevPage = () => {
    if (page > 0) {
      setPage((prev) => prev - 1);
    }
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
    }).format(price);
  };

  if (!restaurantId) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center gap-4">
            <button
              onClick={handleBackToRestaurants}
              className="p-2 hover:bg-gray-100 rounded-full transition-colors"
              aria-label="Back to restaurants"
            >
              <svg
                className="w-6 h-6 text-gray-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M15 19l-7-7 7-7"
                />
              </svg>
            </button>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Menu</h1>
              <p className="mt-1 text-gray-600">
                Browse our delicious offerings
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Search Bar */}
        <div className="mb-8">
          <Input
            type="text"
            placeholder="Search items..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setPage(0);
            }}
            className="max-w-md"
            aria-label="Search items"
          />
        </div>

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            Failed to load items. Please try again.
          </div>
        )}

        {/* Loading Skeleton */}
        {isLoading && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {Array.from({ length: 9 }).map((_, i) => (
              <Skeleton key={i} className="h-64" />
            ))}
          </div>
        )}

        {/* Item Grid */}
        {!isLoading && data && data.content.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {data.content.map((item) => (
              <Card
                key={item.id}
                className="hover:shadow-lg transition-shadow duration-200 flex flex-col"
              >
                <div className="p-6 flex flex-col flex-1">
                  {/* Item Header */}
                  <div className="flex items-start justify-between mb-3">
                    <h3 className="text-lg font-semibold text-gray-900 flex-1">
                      {item.name}
                    </h3>
                    {!item.isAvailable && (
                      <span className="ml-2 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800">
                        Unavailable
                      </span>
                    )}
                  </div>

                  {/* Description */}
                  {item.description && (
                    <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                      {item.description}
                    </p>
                  )}

                  {/* Badges */}
                  <div className="flex flex-wrap gap-2 mb-4">
                    {item.hasVariants && (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        <svg
                          className="w-3 h-3 mr-1"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                        >
                          <path d="M5 3a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2V5a2 2 0 00-2-2H5zM5 11a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2v-2a2 2 0 00-2-2H5zM11 5a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V5zM11 13a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
                        </svg>
                        Variants
                      </span>
                    )}
                    {item.hasAddons && (
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                        <svg
                          className="w-3 h-3 mr-1"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                        >
                          <path
                            fillRule="evenodd"
                            d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z"
                            clipRule="evenodd"
                          />
                        </svg>
                        Add-ons
                      </span>
                    )}
                  </div>

                  {/* Spacer */}
                  <div className="flex-1"></div>

                  {/* Price */}
                  <div className="mt-4 pt-4 border-t border-gray-200">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-xs text-gray-500 mb-1">Starting from</p>
                        <p className="text-2xl font-bold text-red-600">
                          {formatPrice(item.basePrice)}
                        </p>
                      </div>
                      {item.isAvailable && (
                        <Button
                          variant="outline"
                          size="sm"
                          className="border-red-600 text-red-600 hover:bg-red-50"
                          onClick={() => handleItemClick(Number(item.id))}
                          disabled={itemDetailsMutation.isPending}
                        >
                          {itemDetailsMutation.isPending && itemDetailsMutation.variables === Number(item.id)
                            ? 'Loading...'
                            : 'Add to Cart'}
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}

        {/* Empty State */}
        {!isLoading && data && data.content.length === 0 && (
          <div className="text-center py-12">
            <svg
              className="mx-auto h-12 w-12 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
              />
            </svg>
            <h3 className="mt-2 text-lg font-medium text-gray-900">
              No items found
            </h3>
            <p className="mt-1 text-gray-500">
              {searchQuery
                ? 'Try a different search term'
                : 'No items available in this restaurant'}
            </p>
          </div>
        )}

        {/* Pagination */}
        {!isLoading && data && data.content.length > 0 && (
          <div className="mt-8 flex items-center justify-between border-t border-gray-200 pt-6">
            <div className="text-sm text-gray-700">
              Showing page {page + 1} of {data.totalPages}
            </div>
            <div className="flex gap-2">
              <Button
                onClick={handlePrevPage}
                disabled={page === 0}
                variant="outline"
                aria-label="Previous page"
              >
                Previous
              </Button>
              <Button
                onClick={handleNextPage}
                disabled={data.last}
                variant="outline"
                aria-label="Next page"
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* Add to Cart Modal */}
      {itemDetailsMutation.data && (
        <AddToCartModal
          item={itemDetailsMutation.data}
          isOpen={isModalOpen}
          onClose={handleCloseModal}
          onAddToCart={handleAddToCart}
        />
      )}
    </div>
  );
}
