import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { sendOtpViaFirebase } from '../../services/firebase-auth.service';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Loader } from '../../components/ui/Loader';

/**
 * Customer Login Page - OTP Request
 * 
 * Flow:
 * 1. User enters phone/email
 * 2. Request OTP from backend
 * 3. Navigate to OTP verification page
 */
export default function CustomerLoginPage() {
  const [identifier, setIdentifier] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const mutation = useMutation({
    mutationFn: async (phone: string) => {
      // Use Firebase Phone Auth
      await sendOtpViaFirebase(phone);
      return { message: 'OTP sent successfully' };
    },
    onSuccess: () => {
      // Navigate to OTP verification page with phone number
      navigate('/auth/verify-otp', { state: { identifier } });
    },
    onError: (err: any) => {
      setError(err.message || 'Failed to send OTP. Please try again.');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!identifier.trim()) {
      setError('Phone number is required');
      return;
    }

    // Validate phone number format
    const phoneRegex = /^[+]?[0-9]{10,15}$/;
    if (!phoneRegex.test(identifier.replace(/\s/g, ''))) {
      setError('Please enter a valid phone number');
      return;
    }

    mutation.mutate(identifier);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-linear-to-br from-blue-50 via-indigo-50 to-purple-50 px-4 py-12">
      <div className="w-full max-w-md animate-fade-in">
        <div className="bg-white rounded-2xl shadow-2xl p-8 border border-gray-100 backdrop-blur-sm">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-linear-to-br from-red-500 to-red-600 rounded-2xl mb-4 shadow-lg">
              <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2 tracking-tight">
              Welcome to UrbanEats
            </h1>
            <p className="text-gray-600 text-sm">
              Enter your phone number to receive OTP
            </p>
          </div>

          {/* reCAPTCHA Container (invisible) */}
          <div id="recaptcha-container"></div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label
                htmlFor="identifier"
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Phone Number
              </label>
              <Input
                id="identifier"
                type="tel"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                placeholder="+91 98765 43210"
                disabled={mutation.isPending}
                className="text-lg"
              />
            </div>

            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm flex items-start gap-2 animate-shake">
                <svg className="w-5 h-5 shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                <span>{error}</span>
              </div>
            )}

            {/* Submit Button */}
            <Button
              type="submit"
              disabled={mutation.isPending}
              className="w-full"
            >
              {mutation.isPending ? (
                <span className="flex items-center justify-center gap-2">
                  <Loader size="sm" />
                  Sending OTP...
                </span>
              ) : (
                'Continue'
              )}
            </Button>
          </form>

          {/* Development Mode Notice */}
          {import.meta.env.DEV && (
            <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
              <p className="text-xs text-blue-800">
                <strong>🧪 Development Mode:</strong> Using test phone number? No SMS will be sent. 
                Enter your configured test code (e.g., 123456) on the next screen.
              </p>
            </div>
          )}

          {/* Footer */}
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Are you an admin?{' '}
              <button
                type="button"
                onClick={() => navigate('/auth/admin-login')}
                className="text-red-600 hover:text-red-700 font-medium focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 rounded cursor-pointer transition-colors duration-200"
                aria-label="Navigate to admin login page"
              >
                Login here
              </button>
            </p>
          </div>
        </div>

        {/* Additional Info */}
        <div className="mt-6 space-y-3">
          <div className="flex items-center justify-center gap-2 text-xs text-gray-500">
            <svg className="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
            <span>Secure & Encrypted</span>
          </div>
          <p className="text-center text-xs text-gray-500 max-w-sm mx-auto">
            By continuing, you agree to our <button type="button" className="underline hover:text-gray-700 cursor-pointer transition-colors duration-200" aria-label="View terms of service">Terms of Service</button> and <button type="button" className="underline hover:text-gray-700 cursor-pointer transition-colors duration-200" aria-label="View privacy policy">Privacy Policy</button>
          </p>
        </div>
      </div>
    </div>
  );
}
