/**
 * Trigger: onCashWithdrawal
 *
 * Fires when a new cash withdrawal document is created in a group's
 * cash_withdrawals subcollection. Sends a CASH_WITHDRAWAL notification
 * to all group members except the creator.
 */

import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import { CashWithdrawalDoc, NotificationType, FcmDataPayload, NotificationDisplay, NotificationChannelId } from "../types";
import { getRecipientTokens } from "../services/token.service";
import { sendDataMessage } from "../services/notification.service";
import { getGroupData, getActorDisplayName, isGroupBeingDeleted } from "../services/firestore.service";
import { buildDeepLink } from "../utils/format";

export const onCashWithdrawal = onDocumentCreated(
  "groups/{groupId}/cash_withdrawals/{withdrawalId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("onCashWithdrawal: No data in event");
      return;
    }

    const withdrawal = snapshot.data() as CashWithdrawalDoc;
    const groupId = event.params.groupId;
    const withdrawalId = event.params.withdrawalId;
    const actorId = withdrawal.createdBy;

    if (!actorId) {
      logger.warn("onCashWithdrawal: No createdBy field", { groupId, withdrawalId });
      return;
    }

    // Suppress notifications during cascading group deletion
    if (await isGroupBeingDeleted(groupId)) {
      logger.info("onCashWithdrawal: Suppressed — group is being deleted", { groupId, withdrawalId });
      return;
    }

    const [groupData, actorName] = await Promise.all([
      getGroupData(groupId),
      getActorDisplayName(actorId),
    ]);

    if (!groupData) return;

    const tokens = await getRecipientTokens(groupId, actorId, groupData.memberIds);
    if (tokens.length === 0) return;

    const payload: FcmDataPayload = {
      type: NotificationType.CASH_WITHDRAWAL,
      groupId,
      groupName: groupData.name,
      memberName: actorName,
      deepLink: buildDeepLink(groupId, `cash_withdrawals/${withdrawalId}`),
      entityId: withdrawalId,
      amountCents: String(withdrawal.amountWithdrawn),
      currencyCode: withdrawal.currency,
    };

    const display: NotificationDisplay = {
      title: groupData.name,
      titleLocKey: "notification_cash_withdrawal_title",
      bodyLocKey: "notification_cash_withdrawal_body_brief",
      bodyLocArgs: [actorName],
      channelId: NotificationChannelId.FINANCIAL,
    };

    await sendDataMessage(tokens, payload, display);
  }
);
