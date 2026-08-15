# Group Pocket Balance Model & Metrics

This document defines the financial formulas, pocket breakdown metrics, and member balance definitions used in the **SplitTrip** balances dashboard.

---

## 1. Group Pocket Card Overview

The Group Pocket card displays a summary of the group's funds in the base currency (e.g. EUR), broken down into distinct visual figures to give users a clear overview of their pocket status.

```
+----------------------------------------------------+
|                       China                        |
|                                                    |
|                     Remaining                      |
|                     €1,090.59                      |
|                                                    |
|      Available        Scheduled        On hold     |
|       €514.28          €576.31         €154.00     |
|                                                    |
|   Total contributed                     Total spent|
|       €3,335.24                       €2,398.65    |
+----------------------------------------------------+
```

---

## 2. Core Figure Definitions

### 2.1 Remaining
- **Definition**: The net money the group physically or virtually has in the pocket (or group bank account) right now.
- **Formula**: `totalContributions - nonCashExpenses - totalWithdrawals`
- **Key Rule**: Refundable ("On hold") expenses are treated as **already spent** at transaction time, so they are deducted from the Remaining balance.
- **Visual Relation**: `Remaining = Available + Scheduled`

### 2.2 Available
- **Definition**: The money the group can actually spend right now on new expenses.
- **Formula**: `Remaining - Scheduled`
- **Key Rule**: Excludes the funds reserved for scheduled future expenses.

### 2.3 Scheduled
- **Definition**: Money that is currently in the pocket but reserved/held because it is committed to future scheduled expenses (whose due date is in the future).
- **Formula**: Sum of `groupAmount` + add-ons for all active `SCHEDULED` expenses with a future due date.

### 2.4 On hold
- **Definition**: Money already spent on transactions that are marked as `REFUNDABLE` (on hold). 
- **Key Rule**: Since the transaction has already occurred and the funds have left the account, this money is **excluded** from the Remaining and Available balances (subtracted at transaction time). It is displayed as a separate statistics column on the card for informational purposes, indicating the total amount that could potentially be recovered via refunds.

### 2.5 Total Contributed
- **Definition**: The sum of all contributions added to the group pocket by all members.

### 2.6 Total Spent (up to date)
- **Definition**: The total amount spent on the trip across all payment methods.
- **Formula**: Sum of all active (non-cancelled) expenses, including paid/settled expenses, cash-paid expenses, and refundable (on hold) expenses.

---

## 3. Formulas and Invariants

### 3.1 Group Pocket Balance Invariant
```
Remaining (virtualBalance) = Available + Scheduled
```
Where `On Hold` (refundable) non-cash expenses have already been deducted from both `Remaining` and `Available`.

### 3.2 Member Balance Formulas
Each group member's balance mirrors the group-level financial model:
- **`pocketBalance`**: `contributed - withdrawn - nonCashSpent` (non-cash refundable expenses are included in `nonCashSpent` and therefore reduce `pocketBalance`).
- **`cashInHand`**: `withdrawn - cashSpent` (cash refundable expenses are included in `cashSpent` and therefore reduce `cashInHand`).
- **`totalSpent`**: `cashSpent + nonCashSpent` (since refundable spent is already included in cash/non-cash spent).
- **`totalBalance`**: `pocketBalance + cashInHand` (sum across all members equals the group's `virtualBalance + totalCashEquivalent`).
- **`refundableSpent`**: Sum of the member's share of `REFUNDABLE` expenses (displayed separately as "On hold" in the member expanded details view).
