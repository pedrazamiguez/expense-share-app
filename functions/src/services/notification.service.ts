/**
 * Notification dispatch service.
 *
 * Sends FCM messages with both `data` and `android.notification` keys:
 * - `data`: Always included so `onMessageReceived()` fires when the app is alive.
 * - `android.notification`: Includes `titleLocKey`/`bodyLocKey`/`bodyLocArgs`
 *   so that the system tray can render localised notifications even when the
 *   app process is killed or force-stopped.
 *
 * Also handles stale-token cleanup: if a token returns
 * `messaging/registration-token-not-registered`, the corresponding device
 * document is deleted from Firestore.
 */

import * as admin from "firebase-admin";
import { logger } from "firebase-functions/v2";
import { FcmDataPayload, NotificationDisplay } from "../types";

/**
 * Sends an FCM message with data payload and Android notification localization
 * to all provided tokens.
 *
 * @param tokens  - Array of FCM registration tokens
 * @param payload - The data payload to include in the message
 * @param display - Notification display metadata for system-tray rendering
 */
export async function sendDataMessage(
  tokens: string[],
  payload: FcmDataPayload,
  display: NotificationDisplay
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

  // Build Android notification block with localization keys
  const androidNotification: admin.messaging.AndroidNotification = {
    channelId: display.channelId,
    bodyLocKey: display.bodyLocKey,
    ...(display.bodyLocArgs && { bodyLocArgs: display.bodyLocArgs }),
  };

  // Title: use direct text (e.g., group name) or a loc key for localised fallback
  if (display.title) {
    androidNotification.title = display.title;
  } else if (display.titleLocKey) {
    androidNotification.titleLocKey = display.titleLocKey;
  }

  const message: admin.messaging.MulticastMessage = {
    data,
    tokens,
    android: {
      priority: "high" as const,
      notification: androidNotification,
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
 * Chunks deletes into batches of 500 (Firestore batch limit).
 */
async function removeStaleTokens(staleTokens: string[]): Promise<void> {
  const db = admin.firestore();
  const MAX_BATCH_SIZE = 500;
  const docsToDelete: FirebaseFirestore.DocumentReference[] = [];

  for (const token of staleTokens) {
    try {
      const devicesSnap = await db
        .collectionGroup("devices")
        .where("token", "==", token)
        .get();

      devicesSnap.forEach((doc) => {
        docsToDelete.push(doc.ref);
      });
    } catch (err) {
      logger.error("Error querying stale token", { token: token.substring(0, 10) + "...", err });
    }
  }

  if (docsToDelete.length === 0) return;

  // Chunk into batches of 500
  for (let i = 0; i < docsToDelete.length; i += MAX_BATCH_SIZE) {
    const chunk = docsToDelete.slice(i, i + MAX_BATCH_SIZE);
    const batch = db.batch();
    chunk.forEach((ref) => batch.delete(ref));
    await batch.commit();
  }

  logger.info("Deleted stale device documents", { count: docsToDelete.length });
}

