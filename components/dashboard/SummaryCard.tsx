"use client";
import { motion } from "framer-motion";
import type { SummaryCard } from "@/hooks/useDashboardData";

export default function SummaryCard({ card }: { card: SummaryCard }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.25, ease: "easeOut" }}
      className={`glass-card relative overflow-hidden rounded-2xl border-t-[3px] p-5 ${card.borderColor}`}
    >
      <div className="flex items-center gap-2 text-sm text-slate-400">
        {card.icon}
        {card.label}
      </div>
      <p className={`mt-2 text-3xl font-bold tracking-tight ${card.valueColor}`}>
        {card.value}
      </p>
      <p className="mt-1 text-xs text-slate-500">{card.sub}</p>
    </motion.div>
  );
}
