export function formatStorage(mb: number): string {
  if (mb >= 1024) return `${(mb / 1024).toFixed(1)} GB`;
  return `${mb} MB`;
}

export function formatStorageGB(gb: number): string {
  if (gb >= 1024) return `${(gb / 1024).toFixed(0)} TB`;
  return `${gb} GB`;
}
