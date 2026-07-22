import { createGoogleClock } from "@/lib/time/google-clock";

export const googleClock = createGoogleClock({
  syncIntervalMs: 5 * 60 * 1000,
  timeoutMs: 3000,
});

googleClock.start();
