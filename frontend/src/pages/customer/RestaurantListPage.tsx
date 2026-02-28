import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useCustomerStore } from '@/store/customerStore';
import { fetchRestaurants, searchRestaurants } from '@/services/customer.service';
import { Card } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Skeleton } from '@/components/ui/Skeleton';
import { useDebounce } from '@/hooks/useDebounce';

/**
 * Restaurant List Page
 * 
 * Displays restaurants in the selected city with search and pagination.
 * Features:
 * - Filtered by selected city
 * - Search functionality
 * - Rating display
 * - Pagination controls
 */
export default function RestaurantListPage() {
  const navigate = useNavigate();
  const { selectedCityId, setRestaurant } = useCustomerStore();
  
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const debouncedSearch = useDebounce(searchQuery, 300);

  // Redirect if no city selected
  useEffect(() => {
    if (!selectedCityId) {
      navigate('/customer/cities');
    }
  }, [selectedCityId, navigate]);

  // Fetch restaurants or search results
  const { data, isLoading, error } = useQuery({
    queryKey: ['restaurants', selectedCityId, page, debouncedSearch],
    queryFn: () => {
      if (!selectedCityId) throw new Error('No city selected');
      
      return debouncedSearch
        ? searchRestaurants({ q: debouncedSearch, page, size: 10 })
        : fetchRestaurants(selectedCityId, { page, size: 10 });
    },
    enabled: !!selectedCityId,
  });

  const handleRestaurantClick = (restaurantId: string) => {
    setRestaurant(restaurantId);
    navigate(`/customer/items?restaurantId=${restaurantId}`);
  };

  const handleBackToCity = () => {
    navigate('/customer/cities');
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

  const renderRating = (rating: number) => {
    return (
      <div className="flex items-center gap-1">
        <svg
          className="w-5 h-5 text-yellow-400"
          fill="currentColor"
          viewBox="0 0 20 20"
        >
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
        </svg>
        <span className="text-sm font-medium text-gray-900">{rating.toFixed(1)}</span>
      </div>
    );
  };

  if (!selectedCityId) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center gap-4">
            <button
              onClick={handleBackToCity}
              className="p-2 hover:bg-gray-100 rounded-full transition-colors"
              aria-label="Back to cities"
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
              <h1 className="text-3xl font-bold text-gray-900">Restaurants</h1>
              <p className="mt-1 text-gray-600">
                Discover delicious food near you
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
            placeholder="Search restaurants..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setPage(0);
            }}
            className="max-w-md"
            aria-label="Search restaurants"
          />
        </div>

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            Failed to load restaurants. Please try again.
          </div>
        )}

        {/* Loading Skeleton */}
        {isLoading && (
          <div className="space-y-4">
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-40" />
            ))}
          </div>
        )}

        {/* Restaurant List */}
        {!isLoading && data && data.content.length > 0 && (
          <div className="space-y-4">
            {data.content.map((restaurant) => (
              <Card
                key={restaurant.id}
                className="cursor-pointer hover:shadow-lg hover:border-red-600 transition-all duration-200 group"
                onClick={() => handleRestaurantClick(restaurant.id)}
              >
                <div className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h3 className="text-xl font-semibold text-gray-900 group-hover:text-red-600 transition-colors">
                        {restaurant.name}
                      </h3>
                      <p className="mt-2 text-sm text-gray-600">
                        {restaurant.address}
                      </p>
                      {restaurant.landmark && (
                        <p className="mt-1 text-sm text-gray-500">
                          Near {restaurant.landmark}
                        </p>
                      )}
                      <div className="mt-3 flex items-center gap-4">
                        {renderRating(restaurant.rating)}
                        {restaurant.isActive ? (
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                            Open
                          </span>
                        ) : (
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                            Closed
                          </span>
                        )}
                      </div>
                    </div>
                    <svg
                      className="w-6 h-6 text-gray-400 group-hover:text-red-600 transition-colors"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 5l7 7-7 7"
                      />
                    </svg>
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
                d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
              />
            </svg>
            <h3 className="mt-2 text-lg font-medium text-gray-900">
              No restaurants found
            </h3>
            <p className="mt-1 text-gray-500">
              {searchQuery
                ? 'Try a different search term'
                : 'No restaurants available in this city'}
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
    </div>
  );
}
