import type { ZodError } from "zod";

export function parseZodErrors(error: ZodError): Record<string, string> {
  const fieldErrors: Record<string, string> = {};
  error.issues.forEach((issue) => {
    const key = (issue.path[0] as string) || "_root";
    if (!fieldErrors[key]) fieldErrors[key] = issue.message;
  });
  return fieldErrors;
}
