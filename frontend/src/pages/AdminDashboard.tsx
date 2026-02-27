import { Card, CardContent, CardHeader, CardTitle } from '../components/ui';

/**
 * Admin dashboard placeholder.
 * 
 * Foundation only - admin features will be implemented later.
 */
export const AdminDashboard: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-gray-600 mt-2">
          Manage cities, restaurants, and items
        </p>
      </div>

      <div className="grid md:grid-cols-3 gap-6">
        <Card variant="bordered">
          <CardHeader>
            <CardTitle>Cities</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-blue-600">--</p>
            <p className="text-sm text-gray-600 mt-1">Total Cities</p>
          </CardContent>
        </Card>

        <Card variant="bordered">
          <CardHeader>
            <CardTitle>Restaurants</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">--</p>
            <p className="text-sm text-gray-600 mt-1">Total Restaurants</p>
          </CardContent>
        </Card>

        <Card variant="bordered">
          <CardHeader>
            <CardTitle>Menu Items</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-purple-600">--</p>
            <p className="text-sm text-gray-600 mt-1">Total Items</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardContent>
          <div className="text-center py-8">
            <p className="text-gray-600">
              Admin features (CRUD for cities, restaurants, items) will be implemented in dedicated branches.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
