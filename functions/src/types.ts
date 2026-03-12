/**
 * Shared TypeScript types for ExpenseShareApp Cloud Functions.
 *
 * These types mirror the Firestore document shapes defined in the Android
 * codebase (*Document.kt classes) and the FCM data message contract.
 */

// ---------------------------------------------------------------------------
// Notification types — must stay in sync with the Android NotificationType enum
// ---------------------------------------------------------------------------

export enum NotificationType {
  EXPENSE_ADDED = "EXPENSE_ADDED",
  EXPENSE_UPDATED = "EXPENSE_UPDATED",
  EXPENSE_DELETED = "EXPENSE_DELETED",
  MEMBER_ADDED = "MEMBER_ADDED",
  MEMBER_REMOVED = "MEMBER_REMOVED",
  CASH_WITHDRAWAL = "CASH_WITHDRAWAL",
  CONTRIBUTION_ADDED = "CONTRIBUTION_ADDED",
  GROUP_INVITE = "GROUP_INVITE",
  SETTLEMENT_REQUEST = "SETTLEMENT_REQUEST",
  DEFAULT = "DEFAULT",
}

// ---------------------------------------------------------------------------
// Firestore document interfaces (subset of fields needed by Cloud Functions)
// ---------------------------------------------------------------------------

export interface GroupDoc {
  groupId: string;
  name: string;
  currency: string;
  memberIds: string[];
}

export interface GroupMemberDoc {
  memberId: string;
  groupId: string;
  userId: string;
  role: string;
  alias?: string;
}

export interface ExpenseDoc {
  expenseId: string;
  groupId: string;
  title: string;
  description?: string;
  amountCents: number;
  currency: string;
  groupCurrency: string;
  groupAmountCents?: number;
  expenseCategory: string;
  createdBy: string;
  lastUpdatedBy?: string;
}

export interface ContributionDoc {
  contributionId: string;
  groupId: string;
  userId: string;
  amountCents: number;
  currency: string;
  createdBy: string;
}

export interface CashWithdrawalDoc {
  withdrawalId: string;
  groupId: string;
  withdrawnBy: string;
  amountWithdrawn: number;
  currency: string;
  createdBy: string;
}

export interface UserDoc {
  userId: string;
  username: string;
  email: string;
  displayName?: string;
}

export interface DeviceDoc {
  deviceId: string;
  token: string;
  model: string;
}

// ---------------------------------------------------------------------------
// FCM data message payload
// ---------------------------------------------------------------------------

export interface FcmDataPayload {
  type: NotificationType;
  groupId: string;
  groupName: string;
  memberName: string;
  deepLink: string;
  entityId?: string;
  amount?: string;
  expenseTitle?: string;
}

