import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useCustomerStore } from '@/store/customerStore';
import { fetchCities, searchCities } from '@/services/customer.service';
import { Card } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Skeleton } from '@/components/ui/Skeleton';
import { useDebounce } from '@/hooks/useDebounce';

/**
 * City Selection Page
 * 
 * Allows customers to browse and select a city to view restaurants.
 * Features:
 * - Paginated city listing
 * - Search with debouncing
 * - Responsive grid layout
 * - Loading skeletons
 */
export default function CitySelectionPage() {
  const navigate = useNavigate();
  const setCity = useCustomerStore((state) => state.setCity);
  
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const debouncedSearch = useDebounce(searchQuery, 300);

  // Fetch cities or search results
  const { data, isLoading, error } = useQuery({
    queryKey: ['cities', page, debouncedSearch],
    queryFn: () =>
      debouncedSearch
        ? searchCities({ q: debouncedSearch, page, size: 12 })
        : fetchCities(),
  });

  const handleCityClick = (cityId: string) => {
    setCity(cityId);
    navigate('/customer/restaurants');
  };

  const handleNextPage = () => {
    if (data && 'last' in data && !data.last) {
      setPage((prev) => prev + 1);
    }
  };

  const handlePrevPage = () => {
    if (page > 0) {
      setPage((prev) => prev - 1);
    }
  };

  // Determine if data is array (full list) or PageResponse (search results)
  const isArrayData = Array.isArray(data);
  const cities = isArrayData ? data : data?.content || [];
  const hasData = cities.length > 0;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <h1 className="text-3xl font-bold text-gray-900">Select Your City</h1>
          <p className="mt-2 text-gray-600">
            Choose your city to discover amazing restaurants
          </p>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Search Bar */}
        <div className="mb-8">
          <Input
            type="text"
            placeholder="Search cities..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setPage(0); // Reset to first page on search
            }}
            className="max-w-md"
            aria-label="Search cities"
          />
        </div>

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            Failed to load cities. Please try again.
          </div>
        )}

        {/* Loading Skeleton */}
        {isLoading && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {Array.from({ length: 12 }).map((_, i) => (
              <Skeleton key={i} className="h-32" />
            ))}
          </div>
        )}

        {/* City Grid */}
        {!isLoading && hasData && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {cities.map((city) => (
              <Card
                key={city.id}
                className="cursor-pointer hover:shadow-lg hover:border-red-600 transition-all duration-200 group"
                onClick={() => handleCityClick(city.id)}
              >
                <div className="p-6">
                  <h3 className="text-xl font-semibold text-gray-900 group-hover:text-red-600 transition-colors">
                    {city.name}
                  </h3>
                  <p className="mt-2 text-sm text-gray-600">
                    {city.state}, {city.country}
                  </p>
                  <div className="mt-4 flex items-center text-sm text-red-600 opacity-0 group-hover:opacity-100 transition-opacity">
                    <span>Explore restaurants</span>
                    <svg
                      className="ml-2 w-4 h-4"
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
        {!isLoading && data && !hasData && (
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
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
            <h3 className="mt-2 text-lg font-medium text-gray-900">No cities found</h3>
            <p className="mt-1 text-gray-500">
              {searchQuery ? 'Try a different search term' : 'No cities available'}
            </p>
          </div>
        )}

        {/* Pagination - Only show for search results (PageResponse) */}
        {!isLoading && hasData && !isArrayData && (
          <div className="mt-8 flex items-center justify-between border-t border-gray-200 pt-6">
            <div className="text-sm text-gray-700">
              Showing page {page + 1} of {data.totalPages}
            </div>
            <div className="flex gap-2">
              <button
                onClick={handlePrevPage}
                disabled={page === 0}
                className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                aria-label="Previous page"
              >
                Previous
              </button>
              <button
                onClick={handleNextPage}
                disabled={data.last}
                className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                aria-label="Next page"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
