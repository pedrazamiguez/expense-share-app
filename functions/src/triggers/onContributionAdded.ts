/**
 * Trigger: onContributionAdded
 *
 * Fires when a new contribution document is created in a group's
 * contributions subcollection. Sends a CONTRIBUTION_ADDED notification
 * to all group members except the creator.
 */

import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import { ContributionDoc, NotificationType, FcmDataPayload, NotificationDisplay, NotificationChannelId } from "../types";
import { getRecipientTokens } from "../services/token.service";
import { sendDataMessage } from "../services/notification.service";
import { getGroupData, getActorDisplayName } from "../services/firestore.service";
import { buildDeepLink } from "../utils/format";

export const onContributionAdded = onDocumentCreated(
  "groups/{groupId}/contributions/{contributionId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("onContributionAdded: No data in event");
      return;
    }

    const contribution = snapshot.data() as ContributionDoc;
    const groupId = event.params.groupId;
    const contributionId = event.params.contributionId;
    const actorId = contribution.createdBy;

    if (!actorId) {
      logger.warn("onContributionAdded: No createdBy field", { groupId, contributionId });
      return;
    }

    const [groupData, actorName] = await Promise.all([
      getGroupData(groupId),
      getActorDisplayName(actorId),
    ]);

    // Suppress notifications during cascading group deletion (or missing group)
    if (!groupData || groupData.deletionRequested) {
      if (groupData?.deletionRequested) {
        logger.info("onContributionAdded: Suppressed — group is being deleted", { groupId, contributionId });
      }
      return;
    }

    const tokens = await getRecipientTokens(groupId, actorId, groupData.memberIds);
    if (tokens.length === 0) return;

    const payload: FcmDataPayload = {
      type: NotificationType.CONTRIBUTION_ADDED,
      groupId,
      groupName: groupData.name,
      memberName: actorName,
      deepLink: buildDeepLink(groupId, `contributions/${contributionId}`),
      entityId: contributionId,
      amountCents: String(contribution.amountCents),
      currencyCode: contribution.currency,
    };

    const display: NotificationDisplay = {
      title: groupData.name,
      titleLocKey: "notification_contribution_added_title",
      bodyLocKey: "notification_contribution_added_body_brief",
      bodyLocArgs: [actorName],
      channelId: NotificationChannelId.FINANCIAL,
    };

    await sendDataMessage(tokens, payload, display);
  }
);
