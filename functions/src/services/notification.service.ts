/**
 * Notification dispatch service.
 *
 * Sends FCM **data-only** messages (no `notification` key) so that the
 * Android `onMessageReceived()` callback always fires — even in background.
 *
 * Also handles stale-token cleanup: if a token returns
 * `messaging/registration-token-not-registered`, the corresponding device
 * document is deleted from Firestore.
 */

import * as admin from "firebase-admin";
import { logger } from "firebase-functions/v2";
import { FcmDataPayload } from "../types";

/**
 * Sends a data-only FCM message to all provided tokens.
 *
 * @param tokens  - Array of FCM registration tokens
 * @param payload - The data payload to include in the message
 */
export async function sendDataMessage(
  tokens: string[],
  payload: FcmDataPayload
): Promise<void> {
  if (tokens.length === 0) {
    logger.info("No tokens to send to — skipping", { type: payload.type });
    return;
  }

  // Convert all payload values to strings (FCM data must be Record<string, string>)
  const data: Record<string, string> = {};
  for (const [key, value] of Object.entries(payload)) {
    if (value !== undefined && value !== null) {
      data[key] = String(value);
    }
  }

  const message: admin.messaging.MulticastMessage = {
    data,
    tokens,
    // No `notification` key — data-only message
    android: {
      priority: "high" as const,
    },
  };

  logger.info("Sending FCM multicast", {
    type: payload.type,
    recipientCount: tokens.length,
    groupId: payload.groupId,
  });

  const response = await admin.messaging().sendEachForMulticast(message);

  if (response.failureCount > 0) {
    await handleFailedTokens(tokens, response.responses);
  }

  logger.info("FCM send complete", {
    successCount: response.successCount,
    failureCount: response.failureCount,
  });
}

/**
 * Inspects failed send responses and deletes stale device tokens from Firestore.
 */
async function handleFailedTokens(
  tokens: string[],
  responses: admin.messaging.SendResponse[]
): Promise<void> {
  const staleTokens: string[] = [];

  responses.forEach((resp, idx) => {
    if (resp.error) {
      const code = resp.error.code;
      logger.warn("FCM send failed for token", {
        token: tokens[idx].substring(0, 10) + "...",
        errorCode: code,
        errorMessage: resp.error.message,
      });

      if (
        code === "messaging/registration-token-not-registered" ||
        code === "messaging/invalid-registration-token"
      ) {
        staleTokens.push(tokens[idx]);
      }
    }
  });

  if (staleTokens.length > 0) {
    logger.info("Cleaning up stale device tokens", { count: staleTokens.length });
    await removeStaleTokens(staleTokens);
  }
}

/**
 * Deletes device documents whose token matches any of the stale tokens.
 *
 * Scans `users/{uid}/devices` using a collection group query on the `token` field.
 */
async function removeStaleTokens(staleTokens: string[]): Promise<void> {
  const db = admin.firestore();
  const batch = db.batch();
  let deleteCount = 0;

  for (const token of staleTokens) {
    try {
      const devicesSnap = await db
        .collectionGroup("devices")
        .where("token", "==", token)
        .get();

      devicesSnap.forEach((doc) => {
        batch.delete(doc.ref);
        deleteCount++;
      });
    } catch (err) {
      logger.error("Error querying stale token", { token: token.substring(0, 10) + "...", err });
    }
  }

  if (deleteCount > 0) {
    await batch.commit();
    logger.info("Deleted stale device documents", { count: deleteCount });
  }
}

