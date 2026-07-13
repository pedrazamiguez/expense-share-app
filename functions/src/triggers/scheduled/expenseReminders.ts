import * as functions from "firebase-functions/v2";
import * as admin from "firebase-admin";
import { toZonedTime } from "date-fns-tz";
import { addDays } from "date-fns";

export const expenseReminders = functions.scheduler.onSchedule(
  {
    schedule: "every 1 hours",
    timeZone: "UTC",
    retryCount: 3,
  },
  async (event) => {
    const db = admin.firestore();
    const messaging = admin.messaging();

    const expensesQuery = await db
      .collection("expenses")
      .where("paymentStatus", "in", ["SCHEDULED", "REFUNDABLE"])
      .get();

    if (expensesQuery.empty) {
      return;
    }

    const now = new Date();

    for (const doc of expensesQuery.docs) {
      const expense = doc.data();
      if (!expense.dueDate) continue;

      const dueDate = expense.dueDate.toDate();
      const groupId = expense.groupId;
      const expenseId = doc.id;
      const paymentStatus = expense.paymentStatus;

      // Get group members
      const groupDoc = await db.collection("groups").doc(groupId).get();
      if (!groupDoc.exists) continue;
      
      const memberIds = groupDoc.data()?.members || [];
      if (memberIds.length === 0) continue;

      for (const memberId of memberIds) {
        const userDoc = await db.collection("users").doc(memberId).get();
        if (!userDoc.exists) continue;

        const user = userDoc.data()!;
        const timezone = user.timezone || "UTC";
        const preferredReminderTime = user.preferredReminderTime || "12:00";
        
        // Parse the preferred reminder time
        const [prefHour] = preferredReminderTime.split(":").map(Number);
        
        // Check if the current time in the user's timezone matches the preferred hour
        const userNow = toZonedTime(now, timezone);
        
        // We run hourly, so we just check if the hour matches
        if (userNow.getHours() === prefHour) {
            
            // Check if the due date is exactly "tomorrow" in the user's timezone
            const userDueDate = toZonedTime(dueDate, timezone);
            const userTomorrow = addDays(userNow, 1);
            
            if (
                userDueDate.getFullYear() === userTomorrow.getFullYear() &&
                userDueDate.getMonth() === userTomorrow.getMonth() &&
                userDueDate.getDate() === userTomorrow.getDate()
            ) {
                // Fetch FCM tokens (assuming they might be stored in users/fcmTokens collection)
                const tokensSnapshot = await db
                  .collection("users")
                  .doc(memberId)
                  .collection("fcmTokens")
                  .get();

                const tokens = tokensSnapshot.docs.map((t) => t.id);
                if (tokens.length === 0) continue;
                
                const type = paymentStatus === "SCHEDULED" ? "EXPENSE_SCHEDULED_REMINDER" : "EXPENSE_REFUNDABLE_REMINDER";

                const payload = {
                    data: {
                        type,
                        expenseId,
                        groupId
                    },
                    tokens: tokens
                };

                try {
                  const response = await messaging.sendEachForMulticast(payload);
                  if (response.failureCount > 0) {
                      console.warn(`Failed to send ${response.failureCount} reminders for user ${memberId}`);
                  }
                } catch (e) {
                  console.error(`Error sending reminder to user ${memberId}`, e);
                }
            }
        }
      }
    }
  }
);
