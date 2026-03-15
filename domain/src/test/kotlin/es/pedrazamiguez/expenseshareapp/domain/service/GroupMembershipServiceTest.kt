package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.exception.NotGroupMemberException
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GroupMembershipServiceTest {

    private lateinit var groupRepository: GroupRepository
    private lateinit var authenticationService: AuthenticationService
    private lateinit var service: GroupMembershipService

    private val testUserId = "user-123"
    private val testGroupId = "group-456"

    @BeforeEach
    fun setUp() {
        groupRepository = mockk()
        authenticationService = mockk()
        every { authenticationService.requireUserId() } returns testUserId
        service = GroupMembershipService(groupRepository, authenticationService)
    }

    @Nested
    inner class RequireMembership {

        @Test
        fun `passes when user is a member of the group`() = runTest {
            // Given
            val group = Group(
                id = testGroupId,
                name = "Test Group",
                members = listOf(testUserId, "user-other")
            )
            coEvery { groupRepository.getGroupById(testGroupId) } returns group

            // When / Then — no exception thrown
            service.requireMembership(testGroupId)
        }

        @Test
        fun `passes when user is the only member`() = runTest {
            // Given
            val group = Group(
                id = testGroupId,
                name = "Solo Group",
                members = listOf(testUserId)
            )
            coEvery { groupRepository.getGroupById(testGroupId) } returns group

            // When / Then — no exception thrown
            service.requireMembership(testGroupId)
        }

        @Test
        fun `throws NotGroupMemberException when user is not a member`() = runTest {
            // Given
            val group = Group(
                id = testGroupId,
                name = "Other Group",
                members = listOf("user-other-1", "user-other-2")
            )
            coEvery { groupRepository.getGroupById(testGroupId) } returns group

            // When / Then
            try {
                service.requireMembership(testGroupId)
                fail("Expected NotGroupMemberException to be thrown")
            } catch (e: NotGroupMemberException) {
                assertEquals(testGroupId, e.groupId)
                assertEquals(testUserId, e.userId)
            }
        }

        @Test
        fun `throws NotGroupMemberException when group has empty members list`() = runTest {
            // Given
            val group = Group(
                id = testGroupId,
                name = "Empty Group",
                members = emptyList()
            )
            coEvery { groupRepository.getGroupById(testGroupId) } returns group

            // When / Then
            try {
                service.requireMembership(testGroupId)
                fail("Expected NotGroupMemberException to be thrown")
            } catch (e: NotGroupMemberException) {
                assertEquals(testGroupId, e.groupId)
                assertEquals(testUserId, e.userId)
            }
        }

        @Test
        fun `throws NotGroupMemberException when group is not found`() = runTest {
            // Given
            coEvery { groupRepository.getGroupById(testGroupId) } returns null

            // When / Then
            try {
                service.requireMembership(testGroupId)
                fail("Expected NotGroupMemberException to be thrown")
            } catch (e: NotGroupMemberException) {
                assertEquals(testGroupId, e.groupId)
                assertEquals(testUserId, e.userId)
            }
        }

        @Test
        fun `throws IllegalStateException when user is not authenticated`() = runTest {
            // Given
            every { authenticationService.requireUserId() } throws IllegalStateException("Not authenticated")

            // When / Then
            try {
                service.requireMembership(testGroupId)
                fail("Expected IllegalStateException to be thrown")
            } catch (e: IllegalStateException) {
                assertTrue(e.message?.contains("Not authenticated") == true)
            }
        }
    }
}
