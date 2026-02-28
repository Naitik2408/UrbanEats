import { useState, useRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { verifyOtpViaFirebase } from '../../services/firebase-auth.service';
import { verifyOtp } from '../../services/auth.service';
import { useAuthStore } from '../../store/authStore';
import { Button } from '../../components/ui/Button';
import { Loader } from '../../components/ui/Loader';

/**
 * OTP Verification Page
 * 
 * Flow:
 * 1. User enters 6-digit OTP
 * 2. Verify OTP with backend
 * 3. On success: Save token, redirect to customer dashboard
 */
export default function OtpVerificationPage() {
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);
  const setToken = useAuthStore((state) => state.setToken);

  // Get identifier from navigation state
  const identifier = location.state?.identifier;

  useEffect(() => {
    // Redirect back if no identifier
    if (!identifier) {
      navigate('/auth/customer-login');
    }
  }, [identifier, navigate]);

  const mutation = useMutation({
    mutationFn: async (otpString: string) => {
      console.log('🔄 [OTP Verification] Starting verification flow...');
      console.log('📱 [OTP Verification] Phone:', identifier);
      console.log('🔢 [OTP Verification] OTP:', otpString);
      
      // Verify OTP using Firebase
      const firebaseIdToken = await verifyOtpViaFirebase(otpString);
      
      console.log('🔑 [OTP Verification] Firebase token received');
      console.log('📤 [OTP Verification] Sending to backend...');
      
      // Exchange Firebase token for our JWT token
      const response = await verifyOtp({
        identifier: identifier!,
        otp: otpString,
        firebaseToken: firebaseIdToken, // Send Firebase token to backend
      });
      
      console.log('✅ [OTP Verification] Backend response:', response);
      
      return response;
    },
    onSuccess: (data) => {
      console.log('🎉 [OTP Verification] Success! Token:', data.token?.substring(0, 20) + '...');
      console.log('👤 [OTP Verification] Role:', data.role);
      
      // Save token and set role
      setToken(data.token, 'CUSTOMER');
      
      // Redirect to customer dashboard
      console.log('🚀 [OTP Verification] Redirecting to /customer...');
      navigate('/customer', { replace: true });
    },
    onError: (err: any) => {
      console.error('❌ [OTP Verification] Error:', err);
      setError(err.message || 'Invalid OTP. Please try again.');
    },
  });

  const handleOtpChange = (index: number, value: string) => {
    // Only allow digits
    if (value && !/^\d$/.test(value)) {
      return;
    }

    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);
    setError('');

    // Auto-focus next input
    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent) => {
    // Handle backspace
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').trim();
    
    // Only process if it's 6 digits
    if (/^\d{6}$/.test(pastedData)) {
      const newOtp = pastedData.split('');
      setOtp(newOtp);
      inputRefs.current[5]?.focus();
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const otpString = otp.join('');
    
    if (otpString.length !== 6) {
      setError('Please enter all 6 digits');
      return;
    }

    mutation.mutate(otpString);
  };

  const isOtpComplete = otp.every((digit) => digit !== '');

  return (
    <div className="min-h-screen flex items-center justify-center bg-linear-to-br from-blue-50 to-indigo-100 px-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-xl p-8">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-100 rounded-full mb-4">
              <svg
                className="w-8 h-8 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                />
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              Enter OTP
            </h1>
            <p className="text-gray-600">
              We've sent a 6-digit code to
              <br />
              <span className="font-medium text-gray-900">{identifier}</span>
            </p>
            
            {/* Development Mode Notice */}
            {import.meta.env.DEV && (
              <div className="mt-4 p-3 bg-amber-50 border border-amber-200 rounded-lg text-left">
                <p className="text-xs text-amber-800">
                  <strong>💡 Test Mode:</strong> If you added {identifier} as a test number in Firebase Console, 
                  enter the test code you configured (e.g., 123456). No SMS will be sent for test numbers.
                </p>
              </div>
            )}
          </div>

          {/* OTP Input Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="flex justify-center gap-2">
              {otp.map((digit, index) => (
                <input
                  key={index}
                  ref={(el) => {
                    inputRefs.current[index] = el;
                  }}
                  type="text"
                  inputMode="numeric"
                  maxLength={1}
                  value={digit}
                  onChange={(e) => handleOtpChange(index, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(index, e)}
                  onPaste={handlePaste}
                  disabled={mutation.isPending}
                  className="w-12 h-14 text-center text-2xl font-bold border-2 border-gray-300 rounded-lg focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all disabled:bg-gray-100 disabled:cursor-not-allowed"
                  autoFocus={index === 0}
                />
              ))}
            </div>

            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm text-center">
                {error}
              </div>
            )}

            {/* Submit Button */}
            <Button
              type="submit"
              disabled={!isOtpComplete || mutation.isPending}
              className="w-full"
            >
              {mutation.isPending ? (
                <span className="flex items-center justify-center gap-2">
                  <Loader size="sm" />
                  Verifying...
                </span>
              ) : (
                'Verify OTP'
              )}
            </Button>
          </form>

          {/* Footer */}
          <div className="mt-6 text-center">
            <button
              type="button"
              onClick={() => navigate('/auth/customer-login')}
              className="text-sm text-gray-600 hover:text-gray-900 font-medium focus:outline-none focus:underline"
            >
              ← Back to login
            </button>
          </div>
        </div>

        {/* Resend OTP */}
        <div className="text-center mt-6">
          <p className="text-sm text-gray-600">
            Didn't receive the code?{' '}
            <button
              type="button"
              onClick={() => navigate('/auth/customer-login')}
              className="text-blue-600 hover:text-blue-700 font-medium focus:outline-none focus:underline"
            >
              Resend OTP
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
