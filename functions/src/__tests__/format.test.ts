import { formatAmountFromCents, buildDeepLink } from "../utils/format";

describe("formatAmountFromCents", () => {
  it("formats EUR amounts as locale-neutral string", () => {
    const result = formatAmountFromCents(4500, "EUR");
    expect(result).toBe("45.00 EUR");
  });

  it("formats USD amounts as locale-neutral string", () => {
    const result = formatAmountFromCents(1299, "USD");
    expect(result).toBe("12.99 USD");
  });

  it("handles zero amount", () => {
    const result = formatAmountFromCents(0, "EUR");
    expect(result).toBe("0.00 EUR");
  });

  it("handles large amounts", () => {
    const result = formatAmountFromCents(1234567, "EUR");
    expect(result).toBe("12345.67 EUR");
  });

  it("preserves unknown currency codes", () => {
    const result = formatAmountFromCents(1000, "UNKNOWN_CURRENCY");
    expect(result).toBe("10.00 UNKNOWN_CURRENCY");
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

