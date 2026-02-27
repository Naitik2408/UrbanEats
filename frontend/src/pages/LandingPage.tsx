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
          <Link to="/auth">
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
            <div className="text-4xl mb-4">🍔</div>
            <h3 className="text-lg font-semibold mb-2">Wide Selection</h3>
            <p className="text-gray-600">
              Choose from thousands of restaurants and cuisines.
            </p>
          </CardContent>
        </Card>

        <Card variant="elevated">
          <CardContent>
            <div className="text-4xl mb-4">⚡</div>
            <h3 className="text-lg font-semibold mb-2">Fast Delivery</h3>
            <p className="text-gray-600">
              Get your food delivered hot and fresh in minutes.
            </p>
          </CardContent>
        </Card>

        <Card variant="elevated">
          <CardContent>
            <div className="text-4xl mb-4">💳</div>
            <h3 className="text-lg font-semibold mb-2">Easy Payment</h3>
            <p className="text-gray-600">
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
