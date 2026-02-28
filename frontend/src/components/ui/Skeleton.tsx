import type { HTMLAttributes } from 'react';
import clsx from 'clsx';

interface SkeletonProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'text' | 'circular' | 'rectangular';
  width?: string | number;
  height?: string | number;
}

/**
 * Skeleton loading component for better perceived performance
 * 
 * Usage:
 * - <Skeleton variant="text" /> for text lines
 * - <Skeleton variant="circular" width={40} height={40} /> for avatars
 * - <Skeleton variant="rectangular" height={200} /> for images/cards
 */
export const Skeleton = ({
  variant = 'rectangular',
  width,
  height,
  className,
  style,
  ...props
}: SkeletonProps) => {
  const variantStyles = {
    text: 'h-4 rounded',
    circular: 'rounded-full',
    rectangular: 'rounded-lg',
  };

  return (
    <div
      className={clsx(
        'skeleton bg-gray-200 animate-pulse',
        variantStyles[variant],
        className
      )}
      style={{
        width: typeof width === 'number' ? `${width}px` : width,
        height: typeof height === 'number' ? `${height}px` : height,
        ...style,
      }}
      {...props}
    />
  );
};

/**
 * Card skeleton for loading states
 */
export const CardSkeleton = () => (
  <div className="bg-white rounded-lg shadow p-6 space-y-4 animate-fade-in">
    <div className="flex items-center gap-4">
      <Skeleton variant="circular" width={60} height={60} />
      <div className="flex-1 space-y-2">
        <Skeleton variant="text" height={20} width="60%" />
        <Skeleton variant="text" height={16} width="40%" />
      </div>
    </div>
    <Skeleton variant="rectangular" height={120} />
    <div className="flex gap-2">
      <Skeleton variant="rectangular" height={36} width="48%" />
      <Skeleton variant="rectangular" height={36} width="48%" />
    </div>
  </div>
);

/**
 * List skeleton for loading multiple items
 */
export const ListSkeleton = ({ count = 3 }: { count?: number }) => (
  <div className="space-y-4">
    {Array.from({ length: count }).map((_, i) => (
      <CardSkeleton key={i} />
    ))}
  </div>
);
