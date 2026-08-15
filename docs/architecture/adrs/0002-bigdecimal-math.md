# ADR-0002: Strict BigDecimal Math for Financial Calculations

* **Status:** Accepted
* **Deciders:** SplitTrip Engineering Team
* **Date:** 2026-08-15
* **Tags:** domain, math, bigdecimal, precision, currency

---

## Context and Problem Statement

Financial computations involving shared expense splits, currency conversions, add-on distributions, and cash tranche withdrawals are susceptible to floating-point representation errors (IEEE 754 precision loss) when using primitive `Double` or `Float` types. A rounding discrepancy of even a single cent breaks zero-sum balance invariants and undermines user trust.

How do we ensure absolute numerical precision and determinism across all monetary calculations, currency conversions, and boundary serializations?

## Decision Drivers

* Elimination of floating-point precision anomalies (e.g. `0.1 + 0.2 != 0.3`).
* Exact preservation of zero-sum pocket balance invariants.
* Strict rounding mode rules (explicit `RoundingMode` and scale).
* Seamless serialization over SQLite/Room and Firestore document layers.

## Considered Options

1. **Primitive `Double` / `Float`:** Fast and native, but inherently inaccurate due to binary floating-point representation.
2. **Integer Cents Representation (`Long`):** Stores cents as integers, but complicates intermediate percentage calculations, multi-currency conversion rates, and add-on proportions.
3. **`BigDecimal` with Explicit Scale & RoundingMode (Chosen):** Full arbitrary precision for intermediate math, explicit rounding rules for display/storage, and string-based boundary serialization.

## Decision Outcome

Chosen option: **Option 3 (`BigDecimal` with Explicit Scale & RoundingMode)**.

### Architectural Rules
1. **Absolute Prohibition of Float/Double:** `Double` and `Float` are strictly forbidden for money, shares, percentages, add-on amounts, and exchange rates.
2. **Explicit Scale and RoundingMode:** All division, remainder, and rounding operations must specify an explicit scale (typically 2 for display/cents, up to 6 for exchange rates) and `RoundingMode` (typically `HALF_EVEN` or `HALF_UP`).
3. **Boundary Serialization:**
   - Room: Serialized via TypeConverters using `toPlainString()` and `toBigDecimalOrNull()`.
   - Firestore: Serialized as `String` fields to preserve arbitrary precision across cloud platforms.
4. **Formatting Separation:** Domain services and ViewModels must never format `BigDecimal` into human-readable strings. Formatting is the exclusive responsibility of `UiMapper` instances in presentation layers using `LocaleProvider` and `FormattingHelper`.

## Consequences

### Positive
* Zero rounding loss or phantom cents across complex multi-entity and intra-subunit splits.
* Provable mathematical correctness and verifiable zero-sum balance reconciliation.
* Consistent behavior across all Android OS versions, locales, and architectures.

### Negative / Trade-offs
* Object allocation overhead compared to primitive values (negligible for mobile expense tracking throughput).
* Requires disciplined constructor usage (always `BigDecimal("10.00")` or `BigDecimal.valueOf()`, never `BigDecimal(0.1)`).

## References
* [`docs/domain/multi-currency-and-snapshots.md`](../../domain/multi-currency-and-snapshots.md)
* [`docs/domain/group-pockets-and-balances.md`](../../domain/group-pockets-and-balances.md)
* [`docs/architecture/patterns/core-services-catalog.md`](../patterns/core-services-catalog.md) § B
