import { Card, CardContent, CardHeader, CardTitle } from '../components/ui';

/**
 * Auth page placeholder - Login/OTP forms.
 * 
 * Foundation only - actual auth logic will be in feature/frontend-auth branch.
 */
export const AuthPage: React.FC = () => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Authentication</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="text-center py-8">
          <p className="text-gray-600 mb-4">
            Authentication flow placeholder
          </p>
          <p className="text-sm text-gray-500">
            Login and OTP verification will be implemented in the feature/frontend-auth branch.
          </p>
        </div>
      </CardContent>
    </Card>
  );
};
