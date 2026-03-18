/**
 * Shared helpers used by multiple trigger functions.
 */

import * as admin from "firebase-admin";
import { logger } from "firebase-functions/v2";
import { GroupDoc, UserDoc } from "../types";

const db = () => admin.firestore();

/**
 * Checks whether a group is currently being deleted (cascading delete in progress).
 * Used by notification triggers to suppress spam during group deletion.
 *
 * @param groupId - The group ID to check
 * @returns true if the group has `deletionRequested: true`
 */
export async function isGroupBeingDeleted(groupId: string): Promise<boolean> {
  try {
    const groupSnap = await db().collection("groups").doc(groupId).get();
    if (!groupSnap.exists) {
      // Group doc already deleted — treat as deletion in progress
      return true;
    }
    const data = groupSnap.data();
    return data?.deletionRequested === true;
  } catch (err) {
    logger.error("Error checking group deletion status", { groupId, err });
    return false;
  }
}

/**
 * Reads a group document and returns its data.
 * Returns null if the group doesn't exist.
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

