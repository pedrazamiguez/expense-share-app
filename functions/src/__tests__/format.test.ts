import { formatAmountFromCents, buildDeepLink } from "../utils/format";

describe("formatAmountFromCents", () => {
  it("formats EUR amounts correctly", () => {
    const result = formatAmountFromCents(4500, "EUR");
    // Intl.NumberFormat with en-US locale uses "€" symbol
    expect(result).toContain("45.00");
    expect(result).toContain("€");
  });

  it("formats USD amounts correctly", () => {
    const result = formatAmountFromCents(1299, "USD");
    expect(result).toContain("12.99");
    expect(result).toContain("$");
  });

  it("handles zero amount", () => {
    const result = formatAmountFromCents(0, "EUR");
    expect(result).toContain("0.00");
  });

  it("handles large amounts", () => {
    const result = formatAmountFromCents(1234567, "EUR");
    expect(result).toContain("12,345.67");
  });

  it("falls back gracefully for unknown currency codes", () => {
    const result = formatAmountFromCents(1000, "UNKNOWN_CURRENCY");
    expect(result).toContain("10.00");
  });
});

describe("buildDeepLink", () => {
  it("builds a group-level deep link", () => {
    const link = buildDeepLink("group123");
    expect(link).toBe("expenseshareapp://groups/group123");
  });

  it("builds a deep link with sub-path", () => {
    const link = buildDeepLink("group123", "expenses/exp456");
    expect(link).toBe("expenseshareapp://groups/group123/expenses/exp456");
  });

  it("handles empty path", () => {
    const link = buildDeepLink("group123", "");
    // Empty string is falsy → no path appended
    expect(link).toBe("expenseshareapp://groups/group123");
  });
});

