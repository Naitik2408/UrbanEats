import { 
  RecaptchaVerifier, 
  signInWithPhoneNumber,
  type ConfirmationResult 
} from 'firebase/auth';
import { auth } from '../config/firebase';

/**
 * Firebase Phone Authentication Service
 * 
 * Handles OTP sending and verification using Firebase Phone Auth
 */

let recaptchaVerifier: RecaptchaVerifier | null = null;
let confirmationResult: ConfirmationResult | null = null;

/**
 * Initialize reCAPTCHA verifier
 * Must be called before sending OTP
 * 
 * @param containerId - ID of the div element for reCAPTCHA
 */
export const initializeRecaptcha = (containerId: string = 'recaptcha-container'): RecaptchaVerifier => {
  if (!recaptchaVerifier) {
    recaptchaVerifier = new RecaptchaVerifier(auth, containerId, {
      size: 'invisible',
      callback: () => {
        console.log('reCAPTCHA resolved');
      },
      'expired-callback': () => {
        console.log('reCAPTCHA expired');
      }
    });
  }
  return recaptchaVerifier;
};

/**
 * Send OTP to phone number using Firebase
 * 
 * @param phoneNumber - Phone number in E.164 format (e.g., +919876543210)
 * @returns Promise that resolves when OTP is sent
 */
export const sendOtpViaFirebase = async (phoneNumber: string): Promise<void> => {
  try {
    // Ensure phone number is in E.164 format
    const formattedPhone = phoneNumber.startsWith('+') ? phoneNumber : `+91${phoneNumber}`;
    
    console.log('🔍 [Firebase Auth] Starting OTP send process');
    console.log('📱 [Firebase Auth] Phone number:', formattedPhone);
    console.log('🔐 [Firebase Auth] Auth instance:', auth ? 'Initialized' : 'Not initialized');
    
    // Initialize reCAPTCHA if not already done
    const verifier = initializeRecaptcha();
    console.log('🤖 [Firebase Auth] reCAPTCHA verifier:', verifier ? 'Ready' : 'Failed');
    
    // Send OTP
    console.log('📤 [Firebase Auth] Calling signInWithPhoneNumber...');
    confirmationResult = await signInWithPhoneNumber(auth, formattedPhone, verifier);
    
    console.log('✅ [Firebase Auth] OTP sent successfully');
    console.log('📦 [Firebase Auth] Confirmation result:', {
      verificationId: confirmationResult.verificationId ? 'Present' : 'Missing',
      hasConfirmMethod: typeof confirmationResult.confirm === 'function'
    });
  } catch (error: any) {
    console.error('Error sending OTP:', error);
    
    // Reset reCAPTCHA on error
    if (recaptchaVerifier) {
      recaptchaVerifier.clear();
      recaptchaVerifier = null;
    }
    
    throw new Error(error.message || 'Failed to send OTP. Please try again.');
  }
};

/**
 * Verify OTP entered by user
 * 
 * @param otp - 6-digit OTP code
 * @returns Promise that resolves with Firebase ID token
 */
export const verifyOtpViaFirebase = async (otp: string): Promise<string> => {
  try {
    if (!confirmationResult) {
      throw new Error('No OTP request found. Please request OTP first.');
    }
    
    // Verify OTP
    const result = await confirmationResult.confirm(otp);
    
    // Get ID token
    const idToken = await result.user.getIdToken();
    
    console.log('✅ [Firebase Auth] OTP verified successfully');
    console.log('🔑 [Firebase Auth] Firebase ID token obtained:', idToken.substring(0, 20) + '...');
    
    // Clear confirmation result
    confirmationResult = null;
    
    return idToken;
  } catch (error: any) {
    console.error('Error verifying OTP:', error);
    throw new Error(error.message || 'Invalid OTP. Please try again.');
  }
};

/**
 * Cleanup function to reset Firebase auth state
 */
export const resetFirebaseAuth = () => {
  if (recaptchaVerifier) {
    recaptchaVerifier.clear();
    recaptchaVerifier = null;
  }
  confirmationResult = null;
};
