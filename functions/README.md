# ExpenseShareApp — Firebase Cloud Functions

Server-side notification dispatch infrastructure for ExpenseShareApp. These Cloud Functions listen to Firestore document lifecycle events and send FCM push notifications to group members.

## Architecture

```
functions/
├── src/
│   ├── index.ts                    # Entry point — re-exports all triggers
│   ├── types.ts                    # Shared TypeScript interfaces & enums
│   ├── services/
│   │   ├── notification.service.ts # FCM multicast dispatch + stale token cleanup
│   │   ├── token.service.ts        # Device token resolution (group members → FCM tokens)
│   │   └── firestore.service.ts    # Shared Firestore read helpers
│   ├── utils/
│   │   └── format.ts              # Amount formatting + deep link builder
│   ├── triggers/
│   │   ├── onExpenseCreated.ts     # EXPENSE_ADDED
│   │   ├── onExpenseUpdated.ts     # EXPENSE_UPDATED
│   │   ├── onExpenseDeleted.ts     # EXPENSE_DELETED
│   │   ├── onMemberAdded.ts        # MEMBER_ADDED
│   │   ├── onMemberRemoved.ts      # MEMBER_REMOVED
│   │   ├── onCashWithdrawal.ts     # CASH_WITHDRAWAL
│   │   └── onContributionAdded.ts  # CONTRIBUTION_ADDED
│   └── __tests__/                  # Unit tests
├── package.json
├── tsconfig.json
├── jest.config.js
├── .eslintrc.js
└── .prettierrc
```

## Firestore Triggers

| Function | Event | Path | NotificationType |
|---|---|---|---|
| `onExpenseCreated` | `onCreate` | `groups/{groupId}/expenses/{expenseId}` | `EXPENSE_ADDED` |
| `onExpenseUpdated` | `onUpdate` | `groups/{groupId}/expenses/{expenseId}` | `EXPENSE_UPDATED` |
| `onExpenseDeleted` | `onDelete` | `groups/{groupId}/expenses/{expenseId}` | `EXPENSE_DELETED` |
| `onMemberAdded` | `onCreate` | `groups/{groupId}/members/{memberId}` | `MEMBER_ADDED` |
| `onMemberRemoved` | `onDelete` | `groups/{groupId}/members/{memberId}` | `MEMBER_REMOVED` |
| `onCashWithdrawal` | `onCreate` | `groups/{groupId}/cash_withdrawals/{id}` | `CASH_WITHDRAWAL` |
| `onContributionAdded` | `onCreate` | `groups/{groupId}/contributions/{id}` | `CONTRIBUTION_ADDED` |

## FCM Payload Contract

All messages are **data-only** (no `notification` key) so that `onMessageReceived()` fires even when the app is in the background.

### Common fields (all types)

| Key | Type | Description |
|---|---|---|
| `type` | `string` | `NotificationType` enum value |
| `groupId` | `string` | Group where the action occurred |
| `groupName` | `string` | Human-readable group name |
| `memberName` | `string` | Display name of the actor |
| `deepLink` | `string` | Deep link URI for in-app navigation |

### Type-specific fields

| Key | Used by | Description |
|---|---|---|
| `amount` | Expense, CashWithdrawal, Contribution | Formatted amount (e.g. `"€45.00"`) |
| `entityId` | All types | Entity ID for deep link construction |
| `expenseTitle` | Expense events | Title of the expense |

## Prerequisites

- Node.js ≥ 18
- Firebase CLI: `npm install -g firebase-tools`
- Firebase project on the **Blaze** (pay-as-you-go) plan (required for Cloud Functions)
- Authenticated: `firebase login`

## Local Development

```bash
# Install dependencies
cd functions
npm install

# Run linter
npm run lint

# Run unit tests
npm test

# Start local emulator
npm run serve

# View logs
npm run logs
```

## Firebase Emulator

To test functions locally with the Firebase Emulator Suite:

```bash
# From the repo root
firebase emulators:start --only functions,firestore

# In another terminal, use the Firestore emulator UI to create/update/delete
# documents and observe the function logs
```

## Deployment

```bash
# Deploy all functions
npm run deploy

# Or from repo root
firebase deploy --only functions
```

### CI/CD

The `.github/workflows/deploy-functions.yml` workflow automatically deploys functions when changes are pushed to `main` in the `functions/` directory.

**Required GitHub Secret:** `FIREBASE_TOKEN` — generate with `firebase login:ci`.

## Stale Token Cleanup

When an FCM send returns `messaging/registration-token-not-registered`, the function automatically deletes the corresponding device document from `users/{uid}/devices/`. This prevents accumulation of stale tokens over time.

## Key Design Decisions

1. **Data messages only** — Never use FCM `notification` payloads. Data-only ensures `onMessageReceived()` always fires.
2. **Actor exclusion** — The user who triggered the action does NOT receive a notification.
3. **Server-side amount formatting** — Amounts are formatted with currency symbols before sending.
4. **Metadata-only change guard** — `onExpenseUpdated` skips notification if only `lastUpdatedAt`/`lastUpdatedBy` changed.

