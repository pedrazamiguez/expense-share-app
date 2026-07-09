## Big Decimal Math

All decimal math MUST use `BigDecimal` with explicit scale + RoundingMode. `Double` or `Float` are strictly prohibited to prevent IEEE 754 precision loss. Boundary serialization at the Firestore document layer must use `String` (via `toPlainString()` / `toBigDecimalOrNull()`).
