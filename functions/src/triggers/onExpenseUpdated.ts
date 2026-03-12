/**
 * Trigger: onExpenseUpdated
 *
 * Fires when an expense document is updated in a group's expenses subcollection.
 * Sends an EXPENSE_UPDATED notification to all group members except the updater.
 *
 * Includes a meaningful-change guard: skips notification if only metadata fields
 * (lastUpdatedAt, lastUpdatedBy) changed without substantive data changes.
 */

import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import { ExpenseDoc, NotificationType, FcmDataPayload } from "../types";
import { getRecipientTokens } from "../services/token.service";
import { sendDataMessage } from "../services/notification.service";
import { getGroupData, getActorDisplayName } from "../services/firestore.service";
import { formatAmountFromCents, buildDeepLink } from "../utils/format";

/** Fields that do not constitute a "meaningful" change worth notifying about. */
const METADATA_ONLY_FIELDS = new Set(["lastUpdatedAt", "lastUpdatedBy"]);

export const onExpenseUpdated = onDocumentUpdated(
  "groups/{groupId}/expenses/{expenseId}",
  async (event) => {
    const change = event.data;
    if (!change) {
      logger.warn("onExpenseUpdated: No data in event");
      return;
    }

    const before = change.before.data() as ExpenseDoc;
    const after = change.after.data() as ExpenseDoc;
    const groupId = event.params.groupId;
    const expenseId = event.params.expenseId;

    // Skip if only metadata fields changed
    if (isMetadataOnlyChange(before as unknown as Record<string, unknown>, after as unknown as Record<string, unknown>)) {
      logger.info("onExpenseUpdated: Metadata-only change — skipping notification", {
        groupId,
        expenseId,
      });
      return;
    }

    const actorId = after.lastUpdatedBy || after.createdBy;
    if (!actorId) {
      logger.warn("onExpenseUpdated: No actor ID", { groupId, expenseId });
      return;
    }

    const [groupData, actorName, tokens] = await Promise.all([
      getGroupData(groupId),
      getActorDisplayName(actorId),
      getRecipientTokens(groupId, actorId),
    ]);

    if (!groupData || tokens.length === 0) return;

    const currency = after.currency || groupData.currency;
    const amountCents = after.groupAmountCents ?? after.amountCents;

    const payload: FcmDataPayload = {
      type: NotificationType.EXPENSE_UPDATED,
      groupId,
      groupName: groupData.name,
      memberName: actorName,
      deepLink: buildDeepLink(groupId, `expenses/${expenseId}`),
      entityId: expenseId,
      amount: formatAmountFromCents(amountCents, currency),
      expenseTitle: after.title,
    };

    await sendDataMessage(tokens, payload);
  }
);

function isMetadataOnlyChange(
  before: Record<string, unknown>,
  after: Record<string, unknown>
): boolean {
  const allKeys = new Set([...Object.keys(before), ...Object.keys(after)]);

  for (const key of allKeys) {
    if (METADATA_ONLY_FIELDS.has(key)) continue;
    if (JSON.stringify(before[key]) !== JSON.stringify(after[key])) {
      return false;
    }
  }

  return true;
}


