import { useState, useEffect, useCallback, useRef } from 'react';

interface UseCountdownOptions {
  /** Target timestamp (milliseconds since epoch) */
  targetTimestamp: number;
  /** Callback when countdown reaches zero */
  onComplete?: () => void;
  /** Update interval in milliseconds (default: 1000ms) */
  interval?: number;
}

interface CountdownReturn {
  /** Remaining time in milliseconds */
  remainingMs: number;
  /** Formatted time string (mm:ss) */
  formattedTime: string;
  /** Whether countdown has completed */
  isComplete: boolean;
  /** Manually reset countdown to new target */
  reset: (newTarget: number) => void;
}

/**
 * Custom hook for countdown timer with auto-refresh.
 * Used for undo transfer toast countdown display.
 *
 * @example
 * const { formattedTime, isComplete } = useCountdown({
 *   targetTimestamp: Date.now() + 300000, // 5 minutes from now
 *   onComplete: () => toast.dismiss()
 * });
 */
export function useCountdown({
  targetTimestamp,
  onComplete,
  interval = 1000,
}: UseCountdownOptions): CountdownReturn {
  const [remainingMs, setRemainingMs] = useState(0);
  const [isComplete, setIsComplete] = useState(false);
  const onCompleteRef = useRef(onComplete);

  // Update ref to latest callback
  useEffect(() => {
    onCompleteRef.current = onComplete;
  }, [onComplete]);

  // Calculate remaining time
  const calculateRemaining = useCallback(() => {
    const now = Date.now();
    const remaining = Math.max(0, targetTimestamp - now);
    return remaining;
  }, [targetTimestamp]);

  // Initialize and update countdown
  useEffect(() => {
    // Initial calculation
    const remaining = calculateRemaining();
    setRemainingMs(remaining);

    if (remaining === 0) {
      setIsComplete(true);
      onCompleteRef.current?.();
      return;
    }

    // Set up interval for updates
    const intervalId = setInterval(() => {
      const newRemaining = calculateRemaining();
      setRemainingMs(newRemaining);

      if (newRemaining === 0) {
        setIsComplete(true);
        onCompleteRef.current?.();
        clearInterval(intervalId);
      }
    }, interval);

    return () => clearInterval(intervalId);
  }, [targetTimestamp, interval, calculateRemaining]);

  // Format remaining time as mm:ss
  const formattedTime = formatTime(remainingMs);

  // Reset function for manual resets
  const reset = useCallback((newTarget: number) => {
    const remaining = Math.max(0, newTarget - Date.now());
    setRemainingMs(remaining);
    setIsComplete(remaining === 0);
  }, []);

  return {
    remainingMs,
    formattedTime,
    isComplete,
    reset,
  };
}

/**
 * Format milliseconds to mm:ss string.
 */
function formatTime(ms: number): string {
  const totalSeconds = Math.ceil(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;

  return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}
