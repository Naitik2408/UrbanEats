import { Link } from 'react-router-dom';
import { Button, Card, CardContent } from '../components/ui';

/**
 * Landing page - Public homepage.
 * 
 * Foundation only - no business logic.
 */
export const LandingPage: React.FC = () => {
  return (
    <div className="space-y-12">
      {/* Hero Section */}
      <section className="text-center py-12">
        <h1 className="text-5xl font-bold text-gray-900 mb-4">
          Welcome to UrbanEats
        </h1>
        <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
          Your favorite food delivered to your doorstep. Fast, reliable, and delicious.
        </p>
        <div className="flex gap-4 justify-center">
          <Link to="/auth/customer-login">
            <Button size="lg">Get Started</Button>
          </Link>
          <Link to="/customer">
            <Button size="lg" variant="outline">
              Browse Restaurants
            </Button>
          </Link>
        </div>
      </section>

      {/* Features Section */}
      <section className="grid md:grid-cols-3 gap-6">
        <Card variant="elevated">
          <CardContent>
            <div className="inline-flex items-center justify-center w-12 h-12 bg-red-50 rounded-lg mb-4">
              <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold mb-2 text-gray-900">Wide Selection</h3>
            <p className="text-gray-600 text-sm">
              Choose from thousands of restaurants and cuisines.
            </p>
          </CardContent>
        </Card>

        <Card variant="elevated">
          <CardContent>
            <div className="inline-flex items-center justify-center w-12 h-12 bg-yellow-50 rounded-lg mb-4">
              <svg className="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold mb-2 text-gray-900">Fast Delivery</h3>
            <p className="text-gray-600 text-sm">
              Get your food delivered hot and fresh in minutes.
            </p>
          </CardContent>
        </Card>

        <Card variant="elevated">
          <CardContent>
            <div className="inline-flex items-center justify-center w-12 h-12 bg-green-50 rounded-lg mb-4">
              <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold mb-2 text-gray-900">Easy Payment</h3>
            <p className="text-gray-600 text-sm">
              Secure and convenient payment options available.
            </p>
          </CardContent>
        </Card>
      </section>

      {/* CTA Section */}
      <section className="bg-blue-50 rounded-xl p-12 text-center">
        <h2 className="text-3xl font-bold text-gray-900 mb-4">
          Ready to order?
        </h2>
        <p className="text-gray-600 mb-6">
          Sign in to start exploring restaurants near you.
        </p>
        <Link to="/auth">
          <Button size="lg">Sign In Now</Button>
        </Link>
      </section>
    </div>
  );
};
