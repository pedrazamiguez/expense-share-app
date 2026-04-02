## 🐛 Bug Report

### Summary

When logging an expense and using the **AddOn DISCOUNT** with the **"already included"** option, the UI displays an incorrect base cost.

### Steps to Reproduce

1. Start logging a new expense.
2. Add a **DISCOUNT** add-on.
3. Set the discount mode to **"already included"** (i.e., the discount was already applied to the amount paid).
4. Enter **10%** as the discount value.
5. Enter **90 EUR** as the expense amount.
6. Observe the displayed **base cost**.

### Expected Behaviour

The base cost should be **100 EUR**.

> Rationale: if a 10% discount was *already included* in the 90 EUR paid, it means the original price before discount was 100 EUR (90 / 0.9 = 100). The "already included" mode should reverse-calculate the original cost.

### Actual Behaviour

The UI shows a base cost of **81.82 EUR** — which is the result of subtracting 10% *on top of* 90 EUR (90 × 0.9 = 81.82), not reversing the discount.

### Additional Context

- The **"on top"** discount option appears to be working correctly.
- Only the **"already included"** mode is affected.