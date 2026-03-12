/**
 * Trigger: onMemberRemoved
 *
 * Fires when a member document is deleted from a group's members subcollection.
 * Sends a MEMBER_REMOVED notification to all remaining group members.
 *
 * Uses the deleted document's data (before snapshot) to identify the removed member.
 */

import { onDocumentDeleted } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import { GroupMemberDoc, NotificationType, FcmDataPayload } from "../types";
import { getRecipientTokens } from "../services/token.service";
import { sendDataMessage } from "../services/notification.service";
import { getGroupData, getActorDisplayName } from "../services/firestore.service";
import { buildDeepLink } from "../utils/format";

export const onMemberRemoved = onDocumentDeleted(
  "groups/{groupId}/members/{memberId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("onMemberRemoved: No data in event");
      return;
    }

    const member = snapshot.data() as GroupMemberDoc;
    const groupId = event.params.groupId;
    const memberId = event.params.memberId;

    const removedUserId = member.userId;
    if (!removedUserId) {
      logger.warn("onMemberRemoved: No userId in member document", { groupId, memberId });
      return;
    }

    const [groupData, memberDisplayName] = await Promise.all([
      getGroupData(groupId),
      getActorDisplayName(removedUserId),
    ]);

    if (!groupData) return;

    const tokens = await getRecipientTokens(groupId, removedUserId, groupData.memberIds);
    if (tokens.length === 0) return;

    const payload: FcmDataPayload = {
      type: NotificationType.MEMBER_REMOVED,
      groupId,
      groupName: groupData.name,
      memberName: memberDisplayName,
      deepLink: buildDeepLink(groupId),
      entityId: memberId,
    };

    await sendDataMessage(tokens, payload);
  }
);

