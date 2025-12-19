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
  // Calculate initial remaining time synchronously
  const getInitialRemaining = () => Math.max(0, targetTimestamp - Date.now());

  const [remainingMs, setRemainingMs] = useState(getInitialRemaining);
  const [isComplete, setIsComplete] = useState(() => getInitialRemaining() === 0);
  const [prevTarget, setPrevTarget] = useState(targetTimestamp);
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

  // Sync state when target changes (using state to track previous value)
  if (prevTarget !== targetTimestamp) {
    setPrevTarget(targetTimestamp);
    // eslint-disable-next-line react-hooks/purity -- Date.now() is intentional for countdown sync
    const remaining = Math.max(0, targetTimestamp - Date.now());
    setRemainingMs(remaining);
    setIsComplete(remaining === 0);
  }

  // Set up interval for updates
  useEffect(() => {
    const remaining = calculateRemaining();

    if (remaining === 0) {
      onCompleteRef.current?.();
      return;
    }

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
