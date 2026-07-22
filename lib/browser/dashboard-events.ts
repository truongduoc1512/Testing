const DASHBOARD_DATA_CHANGED_EVENT = "vieshop:dashboard-data-changed";

type DashboardDataChangedPayload = {
  source: string;
  at: number;
};

export function notifyDashboardDataChanged(source = "unknown") {
  if (typeof window === "undefined") return;

  const payload: DashboardDataChangedPayload = {
    source,
    at: Date.now(),
  };

  try {
    const channel = new BroadcastChannel(DASHBOARD_DATA_CHANGED_EVENT);
    channel.postMessage(payload);
    channel.close();
  } catch (error) {
    void error;
  }

  try {
    window.localStorage.setItem(DASHBOARD_DATA_CHANGED_EVENT, JSON.stringify(payload));
  } catch (error) {
    void error;
  }
}

export function subscribeDashboardDataChanged(onChange: () => void) {
  if (typeof window === "undefined") return () => {};

  let channel: BroadcastChannel | null = null;
  const handleMessage = () => onChange();
  const handleStorage = (event: StorageEvent) => {
    if (event.key === DASHBOARD_DATA_CHANGED_EVENT) onChange();
  };

  try {
    channel = new BroadcastChannel(DASHBOARD_DATA_CHANGED_EVENT);
    channel.addEventListener("message", handleMessage);
  } catch (error) {
    void error;
  }

  window.addEventListener("storage", handleStorage);

  return () => {
    if (channel) {
      channel.removeEventListener("message", handleMessage);
      channel.close();
    }
    window.removeEventListener("storage", handleStorage);
  };
}
