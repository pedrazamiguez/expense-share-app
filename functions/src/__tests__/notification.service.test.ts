/**
 * Unit tests for notification.service.ts
 *
 * Tests:
 * - Correct FCM multicast message structure (data-only, no notification key)
 * - Stale token cleanup on send failure
 * - Empty token array handling
 */

jest.mock("firebase-admin", () => {
  const sendEachForMulticastMock = jest.fn();
  const batchMock = {
    delete: jest.fn(),
    commit: jest.fn().mockResolvedValue(undefined),
  };
  const collectionGroupMock = jest.fn();

  const firestoreFn = jest.fn(() => ({
    collectionGroup: collectionGroupMock,
    batch: jest.fn(() => batchMock),
  }));

  return {
    firestore: firestoreFn,
    initializeApp: jest.fn(),
    messaging: jest.fn(() => ({
      sendEachForMulticast: sendEachForMulticastMock,
    })),
  };
});

import * as admin from "firebase-admin";
import { sendDataMessage } from "../services/notification.service";
import { NotificationType, FcmDataPayload } from "../types";

describe("notification.service", () => {
  let sendEachForMulticastMock: jest.Mock;
  let collectionGroupMock: jest.Mock;
  let batchDeleteMock: jest.Mock;
  let batchCommitMock: jest.Mock;

  const samplePayload: FcmDataPayload = {
    type: NotificationType.EXPENSE_ADDED,
    groupId: "group123",
    groupName: "Trip to Japan",
    memberName: "Alice",
    deepLink: "expenseshareapp://groups/group123/expenses/exp456",
    entityId: "exp456",
    amount: "€45.00",
    expenseTitle: "Sushi dinner",
  };

  beforeEach(() => {
    jest.clearAllMocks();
    sendEachForMulticastMock = (admin.messaging() as unknown as { sendEachForMulticast: jest.Mock }).sendEachForMulticast;
    const db = admin.firestore() as unknown as {
      collectionGroup: jest.Mock;
      batch: jest.Mock;
    };
    collectionGroupMock = db.collectionGroup;
    const batch = db.batch();
    batchDeleteMock = (batch as unknown as { delete: jest.Mock }).delete;
    batchCommitMock = (batch as unknown as { commit: jest.Mock }).commit;
  });

  it("sends a data-only multicast message with correct payload", async () => {
    sendEachForMulticastMock.mockResolvedValue({
      successCount: 2,
      failureCount: 0,
      responses: [{ success: true }, { success: true }],
    });

    await sendDataMessage(["token1", "token2"], samplePayload);

    expect(sendEachForMulticastMock).toHaveBeenCalledTimes(1);
    const call = sendEachForMulticastMock.mock.calls[0][0];

    // Must be data-only — no notification key
    expect(call.notification).toBeUndefined();
    expect(call.data).toBeDefined();
    expect(call.data.type).toBe("EXPENSE_ADDED");
    expect(call.data.groupId).toBe("group123");
    expect(call.data.groupName).toBe("Trip to Japan");
    expect(call.data.memberName).toBe("Alice");
    expect(call.data.amount).toBe("€45.00");
    expect(call.data.expenseTitle).toBe("Sushi dinner");
    expect(call.tokens).toEqual(["token1", "token2"]);
    expect(call.android.priority).toBe("high");
  });

  it("skips sending when tokens array is empty", async () => {
    await sendDataMessage([], samplePayload);
    expect(sendEachForMulticastMock).not.toHaveBeenCalled();
  });

  it("cleans up stale tokens on registration-token-not-registered error", async () => {
    sendEachForMulticastMock.mockResolvedValue({
      successCount: 1,
      failureCount: 1,
      responses: [
        { success: true },
        {
          success: false,
          error: {
            code: "messaging/registration-token-not-registered",
            message: "Token not registered",
          },
        },
      ],
    });

    // Mock collectionGroup query for stale token lookup
    const staleDocRef = { id: "device1" };
    collectionGroupMock.mockReturnValue({
      where: jest.fn().mockReturnValue({
        get: jest.fn().mockResolvedValue({
          forEach: (cb: (doc: { ref: typeof staleDocRef }) => void) => {
            cb({ ref: staleDocRef });
          },
        }),
      }),
    });

    await sendDataMessage(["good_token", "stale_token"], samplePayload);

    expect(batchDeleteMock).toHaveBeenCalledWith(staleDocRef);
    expect(batchCommitMock).toHaveBeenCalled();
  });

  it("omits undefined payload fields from data map", async () => {
    const minimalPayload: FcmDataPayload = {
      type: NotificationType.MEMBER_ADDED,
      groupId: "group123",
      groupName: "My Group",
      memberName: "Bob",
      deepLink: "expenseshareapp://groups/group123",
    };

    sendEachForMulticastMock.mockResolvedValue({
      successCount: 1,
      failureCount: 0,
      responses: [{ success: true }],
    });

    await sendDataMessage(["token1"], minimalPayload);

    const call = sendEachForMulticastMock.mock.calls[0][0];
    expect(call.data.amount).toBeUndefined();
    expect(call.data.entityId).toBeUndefined();
    expect(call.data.expenseTitle).toBeUndefined();
  });
});

