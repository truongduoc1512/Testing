const OFFSCREEN_WINDOW_POSITION = "-32000,-32000";

export function hiddenChromeWindowArgs(width: number, height: number): string[] {
  return [
    `--window-size=${width},${height}`,
    `--window-position=${OFFSCREEN_WINDOW_POSITION}`,
  ];
}
