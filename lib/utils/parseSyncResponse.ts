export async function extractCreditFromSync(res: Response): Promise<unknown> {
  try {
    const data = await res.json();
    return data?.credit ?? null;
  } catch {
    return null;
  }
}
