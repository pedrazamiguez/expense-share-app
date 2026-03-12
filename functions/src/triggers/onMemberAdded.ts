/**
 * Trigger: onMemberAdded
 *
 * Fires when a new member document is created in a group's members subcollection.
 * Sends a MEMBER_ADDED notification to all existing group members.
 *
 * The new member's userId is used as the "actor" — they don't receive
 * a notification about their own addition.
 */

import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import { GroupMemberDoc, NotificationType, FcmDataPayload } from "../types";
import { getRecipientTokens } from "../services/token.service";
import { sendDataMessage } from "../services/notification.service";
import { getGroupData, getActorDisplayName } from "../services/firestore.service";
import { buildDeepLink } from "../utils/format";

export const onMemberAdded = onDocumentCreated(
  "groups/{groupId}/members/{memberId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("onMemberAdded: No data in event");
      return;
    }

    const member = snapshot.data() as GroupMemberDoc;
    const groupId = event.params.groupId;
    const memberId = event.params.memberId;

    const newMemberUserId = member.userId;
    if (!newMemberUserId) {
      logger.warn("onMemberAdded: No userId in member document", { groupId, memberId });
      return;
    }

    const [groupData, memberDisplayName, tokens] = await Promise.all([
      getGroupData(groupId),
      getActorDisplayName(newMemberUserId),
      getRecipientTokens(groupId, newMemberUserId),
    ]);

    if (!groupData || tokens.length === 0) return;

    const payload: FcmDataPayload = {
      type: NotificationType.MEMBER_ADDED,
      groupId,
      groupName: groupData.name,
      memberName: memberDisplayName,
      deepLink: buildDeepLink(groupId),
      entityId: memberId,
    };

    await sendDataMessage(tokens, payload);
  }
);

