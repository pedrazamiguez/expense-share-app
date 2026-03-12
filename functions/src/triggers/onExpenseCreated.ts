/**
 * Trigger: onExpenseCreated
 *
 * Fires when a new expense document is created in a group's expenses subcollection.
 * Sends an EXPENSE_ADDED notification to all group members except the creator.
 */

import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import { ExpenseDoc, NotificationType, FcmDataPayload } from "../types";
import { getRecipientTokens } from "../services/token.service";
import { sendDataMessage } from "../services/notification.service";
import { getGroupData, getActorDisplayName } from "../services/firestore.service";
import { formatAmountFromCents, buildDeepLink } from "../utils/format";

export const onExpenseCreated = onDocumentCreated(
  "groups/{groupId}/expenses/{expenseId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("onExpenseCreated: No data in event");
      return;
    }

    const expense = snapshot.data() as ExpenseDoc;
    const groupId = event.params.groupId;
    const expenseId = event.params.expenseId;
    const actorId = expense.createdBy;

    if (!actorId) {
      logger.warn("onExpenseCreated: No createdBy field", { groupId, expenseId });
      return;
    }

    const [groupData, actorName, tokens] = await Promise.all([
      getGroupData(groupId),
      getActorDisplayName(actorId),
      getRecipientTokens(groupId, actorId),
    ]);

    if (!groupData || tokens.length === 0) return;

    const currency = expense.currency || groupData.currency;
    const amountCents = expense.groupAmountCents ?? expense.amountCents;

    const payload: FcmDataPayload = {
      type: NotificationType.EXPENSE_ADDED,
      groupId,
      groupName: groupData.name,
      memberName: actorName,
      deepLink: buildDeepLink(groupId, `expenses/${expenseId}`),
      entityId: expenseId,
      amount: formatAmountFromCents(amountCents, currency),
      expenseTitle: expense.title,
    };

    await sendDataMessage(tokens, payload);
  }
);

