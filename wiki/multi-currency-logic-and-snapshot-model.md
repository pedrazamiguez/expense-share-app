## The Problem

Traveling involves spending money in **Transaction Currencies** (THB, USD, MXN) while maintaining a group balance in a **Base Currency** (EUR).

Complexity arises because:

1. **Exchange Rates fluctuate:** The cost of 1000 THB changes hourly.
2. **Payment Methods differ:** Cash uses a fixed rate (ATM withdrawal time); Cards use dynamic bank rates (Revolut, Wise).
3. **Hidden Costs:** Fees and Tips are part of the total but distort the "real" price of items when calculating statistics (e.g., "How much was the pad thai?" vs "How much did we spend on banking fees?").

## The Solution: The Expense Snapshot

To solve this, we treat every Expense as an immutable **financial snapshot**. Once an expense is created, it freezes the relationship between the *Source* (Wallet) and the *Target* (Group Debt) at that specific moment in time.

### 1. The "Holy Trinity" of Data

For every transaction, we store three distinct values to ensure mathematical consistency and auditability:

| Component | Field Name | Type | Description |
| --- | --- | --- | --- |
| **Source** | `sourceAmount` | `Long` | The amount explicitly paid at the terminal (e.g., **500** THB). |
| **Target** | `groupAmount` | `Long` | The normalized value for the group debt (e.g., **13.50** EUR). |
| **Bridge** | `exchangeRate` | `BigDecimal` | The rate used for conversion (e.g., **0.027**). Always `BigDecimal` — never `Double` or `Float`. |

> **Invariant Rule:** `sourceAmount * exchangeRate ≈ groupAmount`
> *Note: We allow slight deviation here if the user manually overrides the `groupAmount` (e.g., forcing the exact Revolut charge).*

### 2. The "Atomic Unit" Standard (Goodbye, Cents)

Calling the field `amountCents` is misleading for currencies like JPY (no decimals) or KWD (3 decimals). We adopt the **Atomic Unit** standard:

* **Storage:** Always store integer `Long`.
* **Logic:** Values are stored in the currency's smallest subdivision.
* **EUR/USD/THB (2 decimals):** Store `100` for 1.00 unit.
* **JPY (0 decimals):** Store `1` for 1 unit.
* **KWD (3 decimals):** Store `1000` for 1.000 unit.


* **Metadata:** Use a `CurrencyRegistry` to know the "Power of 10" for each currency code.

### 3. Handling Fees & Tips (The "Sidecar" Model)

To extract metrics later (e.g., *"We wasted 4% of our budget on ATM fees"*), we separate these from the core price.

* **`rawAmount`**: The price of the item/service.
* **`tipAmount`**: Voluntary addition (e.g., service charge).
* **`feeAmount`**: Involuntary addition (e.g., credit card surcharge).
* **`totalSourceAmount`**: The sum (`raw` + `tip` + `fee`) that actually left the wallet.

**Why?** If you buy a 100 THB beer + 10 THB tip, the "cost of Beer" is 100, but the "debt to Andrés" is 110. Storing them separately allows cool charts later.

---

## Architecture Implementation

### The Domain Model

```kotlin
data class Expense(
    // 1. Identification
    val id: String,
    val description: String,

    // 2. The Source (What you paid in Thailand)
    val sourceCurrency: String,      // "THB"
    val sourceRawAmount: Long,       // 50000 (500.00 THB - Price)
    val sourceTipAmount: Long,       // 5000  (50.00 THB - Tip)
    val sourceFeeAmount: Long,       // 2000  (20.00 THB - Card Fee)
    // Computed property: returns 57000
    val totalSourceAmount: Long get() = sourceRawAmount + sourceTipAmount + sourceFeeAmount,

    // 3. The Target (What the Group owes you in Europe)
    val groupCurrency: String,       // "EUR"
    val totalGroupAmount: Long,      // 1550 (15.50 EUR)

    // 4. The Bridge (The Snapshot)
    val exchangeRate: BigDecimal,    // 0.02719... — ALWAYS BigDecimal, never Double/Float
    
    // 5. Context
    val paymentMethod: PaymentMethod, // CASH, REVOLUT...
    
    // 6. Cash tranches (only for CASH expenses — see below)
    val cashTranches: List<CashTranche> = emptyList()
)
```

> ⚠️ **`exchangeRate` is `BigDecimal`, not `Double`** — this is enforced by the architecture rules to prevent IEEE 754 floating-point precision errors in money calculations. The Firestore document layer serializes it as a `String` (`toPlainString()` / `toBigDecimalOrNull()`).

### The Logic Workflows

#### Scenario A: The "Revolut" (Manual Override)

*You pay 1000 THB. Your Revolut notification immediately says "-27.35 EUR".*

1. **Input:** User enters `1000` (Source) and selects `THB`.
2. **Input:** User sees the app estimated `~27.00 EUR`.
3. **Override:** User types `27.35` into the "Group Cost" field.
4. **Calculation:** App reverse-calculates the rate: `27.35 / 1000 = 0.02735`.
5. **Save:** We save the explicit `27.35` EUR and the implied rate.

#### Scenario B: The "Cash" (FIFO ATM Rate)

*You withdrew 10,000 THB at an airport ATM at 0.027 EUR/THB. Later, you withdraw 2,000 THB at a local ATM at 0.028 EUR/THB. Today the group buys 6,000 THB of water park tickets paid in cash.*

The app does **not** ask the user to enter a rate. Instead, it uses the **FIFO (First-In-First-Out) algorithm**:

1. **Input:** User enters `6,000` THB as the source amount and selects `CASH`.
2. **Preview:** The app shows a live "Funded from" breakdown:
   ```
   Funded from:
     • Airport ATM (Jan 10)   →  5,000 THB  @  0.027 EUR/THB
     • Local ATM (Jan 14)     →  1,000 THB  @  0.028 EUR/THB
   Rate: 1 THB = 0.0272 EUR (blended)   [Indicative]
   ```
3. **Save:** FIFO runs — 5,000 THB consumed from the airport withdrawal, 1,000 THB from the local ATM. Each withdrawal's `remainingAmount` is updated. The expense stores `cashTranches` linking it to both withdrawals.
4. **Delete (if needed):** Deleting the expense refunds both tranches, restoring `remainingAmount` on each withdrawal.

> **Scope-aware FIFO:** If the expense was a personal expense (`payerType = USER`), the FIFO algorithm queries the user's personal withdrawal pool first, then falls back to the group pool if insufficient. See [Cash Tranche FIFO & Withdrawal Pools](cash-tranche-fifo-and-withdrawal-pools.md) for the full FIFO architecture.

## Future Analytics (The Payoff)

Because we structured the data this way, we can answer questions like:

* *"What was our effective exchange rate average across the whole trip?"* (Average of all `exchangeRate` fields).
* *"How much did we spend on Tips?"* (Sum of `sourceTipAmount` converted to Base Currency).
* *"Was the Cash rate better than the Revolut rate?"* (Compare `exchangeRate` grouped by `paymentMethod`).
* *"Which ATM gave us the best rate?"* (Aggregate `exchangeRate` across `cashTranches` per withdrawal).

---

## See Also

- [Cash Tranche FIFO & Withdrawal Pools](cash-tranche-fifo-and-withdrawal-pools.md) — full FIFO lifecycle, scope-aware pool selection, tranche preview UI, conflict detection
- [Subunits & Group Composition](sub-units-and-group-composition.md) — how `withdrawalScope` (GROUP/SUBUNIT/USER) affects the FIFO pool
