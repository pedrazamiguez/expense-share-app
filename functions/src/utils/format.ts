/**
 * Amount formatting utilities for FCM notification payloads.
 *
 * Amounts are stored as cents (Long) in Firestore. This helper converts them
 * to human-readable strings with the currency code so the notification is
 * useful without client-side processing.
 */

/**
 * Converts an amount in cents to a human-readable currency string.
 *
 * @param amountCents - Amount in the smallest currency unit (e.g. 4500 = 45.00)
 * @param currency    - ISO 4217 currency code (e.g. "EUR", "USD")
 * @returns Formatted string such as "€45.00" or "45.00 USD"
 */
export function formatAmountFromCents(amountCents: number, currency: string): string {
  const amount = amountCents / 100;

  try {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    // Fallback if the currency code is not recognised by Intl
    return `${amount.toFixed(2)} ${currency}`;
  }
}

/**
 * Builds a deep link URI for in-app navigation.
 *
 * @param groupId  - The group ID
 * @param path     - Optional sub-path (e.g. "expenses/exp_123")
 * @returns Deep link string like "expenseshareapp://groups/abc123/expenses/exp_456"
 */
export function buildDeepLink(groupId: string, path?: string): string {
  const base = `expenseshareapp://groups/${groupId}`;
  return path ? `${base}/${path}` : base;
}

