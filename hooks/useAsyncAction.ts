"use client";

import { useCallback, useState } from "react";

export function useAsyncAction<TArgs extends unknown[], TResult>(
  action: (...args: TArgs) => Promise<TResult>,
) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const run = useCallback(
    async (...args: TArgs) => {
      setLoading(true);
      setError(null);
      try {
        return await action(...args);
      } catch (e) {
        const message = e instanceof Error ? e.message : "Yêu cầu thất bại";
        setError(message);
        throw e;
      } finally {
        setLoading(false);
      }
    },
    [action],
  );

  return { loading, error, run, setError };
}
