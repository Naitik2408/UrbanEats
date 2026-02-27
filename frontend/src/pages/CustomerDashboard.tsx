import { Card, CardContent, CardHeader, CardTitle } from '../components/ui';

/**
 * Customer dashboard placeholder.
 * 
 * Foundation only - customer features will be implemented later.
 */
export const CustomerDashboard: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Browse & Order</h1>
        <p className="text-gray-600 mt-2">
          Explore restaurants and place orders
        </p>
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <Card variant="bordered">
          <CardHeader>
            <CardTitle>My Cart</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-blue-600">0</p>
            <p className="text-sm text-gray-600 mt-1">Items in cart</p>
          </CardContent>
        </Card>

        <Card variant="bordered">
          <CardHeader>
            <CardTitle>My Orders</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">0</p>
            <p className="text-sm text-gray-600 mt-1">Total orders</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardContent>
          <div className="text-center py-8">
            <p className="text-gray-600">
              Customer features (browse restaurants, cart management, order placement) will be implemented in dedicated branches.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
