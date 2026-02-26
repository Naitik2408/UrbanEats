import { useEffect, useState } from 'react';
import { publicService } from '@/services/public.service';
import { type HealthResponse } from '@/types/api.types';

function HomePage() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const checkHealth = async () => {
      try {
        const response = await publicService.checkHealth();
        setHealth(response);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        setError(err.detail || 'Failed to connect to backend');
      } finally {
        setLoading(false);
      }
    };

    checkHealth();
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="card max-w-md w-full text-center">
        <h1 className="text-4xl font-bold text-primary-600 mb-4">
          UrbanEats
        </h1>
        <p className="text-gray-600 mb-6">
          Multi-City Food Ordering Platform
        </p>

        {loading && (
          <div className="text-gray-500">Checking backend connection...</div>
        )}

        {error && (
          <div className="bg-red-50 text-red-600 p-4 rounded-lg">
            <p className="font-semibold">Backend Connection Failed</p>
            <p className="text-sm">{error}</p>
          </div>
        )}

        {health && (
          <div className="bg-green-50 text-green-600 p-4 rounded-lg">
            <p className="font-semibold">✓ Backend Connected</p>
            <p className="text-sm">Status: {health.status}</p>
            <p className="text-sm">Service: {health.service}</p>
          </div>
        )}

        <div className="mt-6 pt-6 border-t border-gray-200">
          <p className="text-sm text-gray-500">
            Infrastructure Setup Complete
          </p>
          <p className="text-xs text-gray-400 mt-2">
            Branch: feature/setup
          </p>
        </div>
      </div>
    </div>
  );
}

export default HomePage;
