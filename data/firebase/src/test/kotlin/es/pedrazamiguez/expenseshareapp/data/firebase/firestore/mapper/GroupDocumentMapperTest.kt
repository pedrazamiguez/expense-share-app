package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.firestore.DocumentReference
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GroupDocumentMapperTest {

    private val testGroupId = "group-123"
    private val testUserId = "user-456"
    private val testTimestamp = LocalDateTime.of(2026, 1, 15, 12, 30, 0)
    private val testFirebaseTimestamp = testTimestamp.toTimestampUtc()!!

    private val fullGroup = Group(
        id = testGroupId,
        name = "Trip to Japan",
        description = "Travel expenses",
        currency = "EUR",
        extraCurrencies = listOf("JPY", "THB"),
        members = listOf("user-1", "user-2", "user-3"),
        mainImagePath = "/images/japan.jpg",
        createdAt = testTimestamp,
        lastUpdatedAt = testTimestamp
    )

    // ...existing ToDocument tests...

    // ...existing ToDomain tests...

    @Nested
    inner class ToAdminMemberDocumentTest {

        private val groupDocRef = mockk<DocumentReference> {
            every { id } returns testGroupId
        }

        @Test
        fun `sets userId and memberId to provided userId`() {
            val doc = toAdminMemberDocument(groupDocRef, testUserId)

            assertEquals(testUserId, doc.userId)
            assertEquals(testUserId, doc.memberId)
        }

        @Test
        fun `sets role to ADMIN`() {
            val doc = toAdminMemberDocument(groupDocRef, testUserId)

            assertEquals("ADMIN", doc.role)
        }

        @Test
        fun `sets groupId and groupRef from document reference`() {
            val doc = toAdminMemberDocument(groupDocRef, testUserId)

            assertEquals(testGroupId, doc.groupId)
            assertEquals(groupDocRef, doc.groupRef)
        }

        @Test
        fun `defaults addedBy to userId when not specified`() {
            val doc = toAdminMemberDocument(groupDocRef, testUserId)

            assertEquals(testUserId, doc.addedBy)
        }

        @Test
        fun `uses explicit addedBy when provided`() {
            val adminUserId = "admin-789"
            val doc = toAdminMemberDocument(groupDocRef, testUserId, addedBy = adminUserId)

            assertEquals(adminUserId, doc.addedBy)
            assertEquals(testUserId, doc.userId)
        }
    }

    @Nested
    inner class ToRegularMemberDocumentTest {

        private val groupDocRef = mockk<DocumentReference> {
            every { id } returns testGroupId
        }
        private val memberId = "member-789"
        private val addedByUserId = "admin-456"

        @Test
        fun `sets userId and memberId to provided memberId`() {
            val doc = toRegularMemberDocument(groupDocRef, memberId, addedBy = addedByUserId)

            assertEquals(memberId, doc.userId)
            assertEquals(memberId, doc.memberId)
        }

        @Test
        fun `sets role to MEMBER`() {
            val doc = toRegularMemberDocument(groupDocRef, memberId, addedBy = addedByUserId)

            assertEquals("MEMBER", doc.role)
        }

        @Test
        fun `sets groupId and groupRef from document reference`() {
            val doc = toRegularMemberDocument(groupDocRef, memberId, addedBy = addedByUserId)

            assertEquals(testGroupId, doc.groupId)
            assertEquals(groupDocRef, doc.groupRef)
        }

        @Test
        fun `sets addedBy to the user who added the member`() {
            val doc = toRegularMemberDocument(groupDocRef, memberId, addedBy = addedByUserId)

            assertEquals(addedByUserId, doc.addedBy)
        }

        @Test
        fun `addedBy differs from memberId when admin adds another user`() {
            val doc = toRegularMemberDocument(groupDocRef, memberId, addedBy = addedByUserId)

            assertEquals(addedByUserId, doc.addedBy)
            assertEquals(memberId, doc.userId)
            assertTrue(doc.addedBy != doc.userId)
        }
    }
}

