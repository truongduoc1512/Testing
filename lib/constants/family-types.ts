import { Crown, Youtube } from "lucide-react";

export const FAMILY_TYPES = [
  {
    id: "ultra",
    label: "Fam Ultra",
    icon: Crown,
    color: "text-violet-400",
    bg: "bg-violet-500/15 border-violet-500/30",
    activeClass: "border-violet-500 bg-violet-500/15 text-violet-300",
  },
  {
    id: "pro",
    label: "Fam Pro",
    icon: Crown,
    color: "text-cyan-400",
    bg: "bg-cyan-500/15 border-cyan-500/30",
    activeClass: "border-cyan-500 bg-cyan-500/15 text-cyan-300",
  },
  {
    id: "youtube",
    label: "Fam YouTube",
    icon: Youtube,
    color: "text-red-400",
    bg: "bg-red-500/15 border-red-500/30",
    activeClass: "border-red-500 bg-red-500/15 text-red-300",
  },
] as const;

export type FamilyTypeId = (typeof FAMILY_TYPES)[number]["id"];
