export function normalizeAccountStatus(status: string | null | undefined): string {
  return (status || "").trim().toLowerCase();
}

export function isSyncableAccountStatus(status: string | null | undefined): boolean {
  return normalizeAccountStatus(status) !== "dead";
}
