/**
 * Shared helpers used by multiple trigger functions.
 */

import * as admin from "firebase-admin";
import { logger } from "firebase-functions/v2";
import { GroupDoc, UserDoc, SettlementDoc } from "../types";

const db = () => admin.firestore();

/**
 * Reads a group document and returns its data.
 * Returns null if the group doesn't exist.
 *
 * Callers can check `groupData.deletionRequested` to suppress
 * notifications during cascading group deletion.
 */
export async function getGroupData(groupId: string): Promise<GroupDoc | null> {
  const groupSnap = await db().collection("groups").doc(groupId).get();

  if (!groupSnap.exists) {
    logger.warn("Group document not found", { groupId });
    return null;
  }

  return groupSnap.data() as GroupDoc;
}

/**
 * Reads a user document and returns the display name.
 * Falls back to username → email → "Someone" if display name is not set.
 */
export async function getActorDisplayName(userId: string): Promise<string> {
  try {
    const userSnap = await db().collection("users").doc(userId).get();

    if (!userSnap.exists) {
      logger.warn("User document not found", { userId });
      return "Someone";
    }

    const user = userSnap.data() as UserDoc;
    return user.displayName || user.username || user.email || "Someone";
  } catch (err) {
    logger.error("Error fetching user display name", { userId, err });
    return "Someone";
  }
}

/**
 * Reads a settlement document and returns its data.
 * Returns null if the settlement doesn't exist.
 */
export async function getSettlementData(
  groupId: string,
  settlementId: string
): Promise<SettlementDoc | null> {
  try {
    const settlementSnap = await db()
      .collection("groups")
      .doc(groupId)
      .collection("settlements")
      .doc(settlementId)
      .get();

    if (!settlementSnap.exists) {
      logger.warn("Settlement document not found", { groupId, settlementId });
      return null;
    }

    return settlementSnap.data() as SettlementDoc;
  } catch (err) {
    logger.error("Error fetching settlement document", { groupId, settlementId, err });
    return null;
  }
}
