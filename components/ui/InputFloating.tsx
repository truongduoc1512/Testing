"use client";
import { forwardRef, useId } from "react";
import { cn } from "@/lib/utils";
import { InfoCircleIcon } from "@/components/icons";

export interface InputFloatingProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

const InputFloating = forwardRef<HTMLInputElement, InputFloatingProps>(
  ({ label, error, className, value, ...props }, ref) => {
    const fallbackId = useId();
    const id = props.id ?? fallbackId;
    const hasValue = typeof value === "string" ? value.length > 0 : false;

    return (
      <div>
        <div
          className={cn("floating-group", hasValue && "has-value", error && "has-error")}
        >
          <input
            ref={ref}
            id={id}
            placeholder=" "
            value={value}
            className={cn("floating-input", className)}
            {...props}
          />
          <label htmlFor={id} className="floating-label">
            {label}
          </label>
          <fieldset className="floating-fieldset" aria-hidden="true">
            <legend className="floating-legend">
              <span>{label}</span>
            </legend>
          </fieldset>
        </div>

        {error && (
          <p className="field-error">
            <InfoCircleIcon className="h-3.5 w-3.5 flex-shrink-0" />
            {error}
          </p>
        )}
      </div>
    );
  },
);

InputFloating.displayName = "InputFloating";

export { InputFloating };
