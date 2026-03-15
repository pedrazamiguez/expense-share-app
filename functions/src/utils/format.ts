/**
 * Amount formatting utilities for FCM notification payloads.
 *
 * Amounts are stored as cents (Long) in Firestore. This helper converts them
 * to human-readable strings for FCM `bodyLocArgs`.
 *
 * IMPORTANT: These values are substituted into Android string resources via
 * `body_loc_args` for system-tray notifications (app killed/background).
 * The server does NOT know the recipient's locale, so we use a locale-neutral
 * format: plain decimal + ISO currency code (e.g., "6.00 EUR").
 *
 * When the app is in the foreground, the Android notification handler
 * (`NotificationAmountFormatter`) re-formats with the device's locale,
 * producing properly localised output (e.g., "6,00 €" for Spanish).
 */

/**
 * Converts an amount in cents to a locale-neutral currency string.
 *
 * @param amountCents - Amount in the smallest currency unit (e.g. 4500 = 45.00)
 * @param currency    - ISO 4217 currency code (e.g. "EUR", "USD")
 * @returns Locale-neutral string such as "45.00 EUR" or "12.99 USD"
 */
export function formatAmountFromCents(amountCents: number, currency: string): string {
  const amount = (amountCents / 100).toFixed(2);
  return `${amount} ${currency}`;
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

