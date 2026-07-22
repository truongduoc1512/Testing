"use client";
import { forwardRef } from "react";
import { cn } from "@/lib/utils";

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "gradient" | "ghost" | "outline";
  size?: "sm" | "md" | "lg";
  loading?: boolean;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      className,
      variant = "gradient",
      size = "md",
      loading = false,
      disabled,
      children,
      ...props
    },
    ref,
  ) => {
    const base =
      "relative inline-flex items-center justify-center gap-2 font-bold transition-all active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-80 overflow-hidden";

    const variants = {
      gradient: "btn-gradient text-white rounded-xl",
      ghost: "text-slate-400 hover:text-white hover:bg-white/5 rounded-xl",
      outline:
        "border border-glass-border text-slate-300 hover:bg-white/5 hover:border-white/20 rounded-xl",
    };

    const sizes = {
      sm: "px-4 py-2 text-sm",
      md: "px-6 py-4 text-base",
      lg: "px-8 py-5 text-lg",
    };

    return (
      <button
        ref={ref}
        disabled={disabled || loading}
        className={cn(base, variants[variant], sizes[size], className)}
        {...props}
      >
        {loading ? (
          <span className="inline-block h-5 w-5 animate-spin rounded-full border-2 border-white/30 border-t-white" />
        ) : (
          children
        )}
      </button>
    );
  },
);
Button.displayName = "Button";

export { Button };
