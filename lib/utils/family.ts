export function getMaxMembers(familyType: string | null | undefined): number {
  return familyType === "gpt" ? 5 : 6;
}

export function isUltraType(familyType: string | null | undefined): boolean {
  return (familyType || "ultra") === "ultra";
}

export function isGoogleType(familyType: string | null | undefined): boolean {
  const t = familyType || "ultra";
  return t !== "gpt" && t !== "youtube";
}
