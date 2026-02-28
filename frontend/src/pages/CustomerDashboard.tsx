import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * Customer dashboard - redirects to city selection.
 * 
 * Entry point for customer browsing flow.
 */
export const CustomerDashboard: React.FC = () => {
  const navigate = useNavigate();

  useEffect(() => {
    // Redirect to city selection page
    navigate('/customer/cities', { replace: true });
  }, [navigate]);

  return null;
};
