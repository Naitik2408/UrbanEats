import { forwardRef } from 'react';
import type { InputHTMLAttributes } from 'react';
import clsx from 'clsx';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

/**
 * Reusable Input component following UI/UX best practices.
 * 
 * Features:
 * - Label support
 * - Error state with message
 * - Helper text
 * - Accessible focus states
 * - Disabled state styling
 */
export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, className, id, ...props }, ref) => {
    const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            {label}
            {props.required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}
        
        <input
          ref={ref}
          id={inputId}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={error ? `${inputId}-error` : helperText ? `${inputId}-helper` : undefined}
          className={clsx(
            'w-full px-4 py-2.5 border rounded-lg transition-all duration-200',
            'focus:outline-none focus:ring-2 focus:ring-offset-0',
            'placeholder:text-gray-400',
            error
              ? 'border-red-300 focus:ring-red-500 focus:border-red-500 bg-red-50/30'
              : 'border-gray-300 focus:ring-red-500 focus:border-red-500 hover:border-gray-400',
            props.disabled && 'bg-gray-50 cursor-not-allowed opacity-60',
            className
          )}
          {...props}
        />
        
        {error && (
          <p id={`${inputId}-error`} className="mt-1.5 text-sm text-red-600 flex items-center gap-1.5" role="alert">
            <svg className="w-4 h-4 shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
            {error}
          </p>
        )}
        
        {helperText && !error && (
          <p id={`${inputId}-helper`} className="mt-1.5 text-sm text-gray-500">{helperText}</p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
