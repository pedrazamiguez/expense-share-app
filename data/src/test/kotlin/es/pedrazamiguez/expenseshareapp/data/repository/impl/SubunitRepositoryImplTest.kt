package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudSubunitDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalSubunitDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("SubunitRepositoryImpl")
class SubunitRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var cloudSubunitDataSource: CloudSubunitDataSource
    private lateinit var localSubunitDataSource: LocalSubunitDataSource
    private lateinit var authenticationService: AuthenticationService
    private lateinit var repository: SubunitRepositoryImpl

    private val testGroupId = "group-123"
    private val testUserId = "user-1"

    private val testSubunit = Subunit(
        id = "subunit-1",
        groupId = testGroupId,
        name = "Antonio & Me",
        memberIds = listOf("user-1", "user-2"),
        memberShares = mapOf("user-1" to 0.5, "user-2" to 0.5),
        createdBy = testUserId,
        createdAt = LocalDateTime.of(2026, 3, 13, 12, 0)
    )

    private val cloudSubunits = listOf(
        Subunit(
            id = "cloud-sub-1",
            groupId = testGroupId,
            name = "Couple A",
            memberIds = listOf("user-1", "user-2"),
            memberShares = mapOf("user-1" to 0.5, "user-2" to 0.5),
            createdBy = "user-1",
            createdAt = LocalDateTime.of(2026, 3, 10, 10, 0)
        ),
        Subunit(
            id = "cloud-sub-2",
            groupId = testGroupId,
            name = "Family B",
            memberIds = listOf("user-3", "user-4", "user-5"),
            memberShares = mapOf("user-3" to 0.4, "user-4" to 0.3, "user-5" to 0.3),
            createdBy = "user-3",
            createdAt = LocalDateTime.of(2026, 3, 11, 14, 0)
        )
    )

    @BeforeEach
    fun setUp() {
        cloudSubunitDataSource = mockk(relaxed = true)
        localSubunitDataSource = mockk(relaxed = true)
        authenticationService = mockk()

        coEvery { authenticationService.currentUserId() } returns testUserId

        repository = SubunitRepositoryImpl(
            cloudSubunitDataSource = cloudSubunitDataSource,
            localSubunitDataSource = localSubunitDataSource,
            authenticationService = authenticationService,
            ioDispatcher = testDispatcher
        )
    }

    @Nested
    @DisplayName("CreateSubunit")
    inner class CreateSubunit {

        @Test
        fun `saves to local storage first`() = runTest(testDispatcher) {
            // Given
            val subunit = Subunit(name = "New Couple", memberIds = listOf("u1", "u2"))
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs

            // When
            repository.createSubunit(testGroupId, subunit)

            // Then - Local save should happen immediately
            coVerify(exactly = 1) { localSubunitDataSource.saveSubunit(any()) }
        }

        @Test
        fun `syncs to cloud in background`() = runTest(testDispatcher) {
            // Given
            val subunit = Subunit(name = "New Couple", memberIds = listOf("u1", "u2"))
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs
            coEvery { cloudSubunitDataSource.addSubunit(any(), any()) } just Runs

            // When
            repository.createSubunit(testGroupId, subunit)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) {
                cloudSubunitDataSource.addSubunit(testGroupId, any())
            }
        }

        @Test
        fun `generates UUID when id is blank`() = runTest(testDispatcher) {
            // Given
            val subunit = Subunit(id = "", name = "New Couple")
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs

            // When
            val returnedId = repository.createSubunit(testGroupId, subunit)

            // Then - Saved subunit should have a non-blank ID
            assertTrue(returnedId.isNotBlank())
            coVerify {
                localSubunitDataSource.saveSubunit(match { it.id.isNotBlank() })
            }
        }

        @Test
        fun `sets createdBy from authentication service when blank`() = runTest(testDispatcher) {
            // Given
            val subunit = Subunit(createdBy = "", name = "New Couple")
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs

            // When
            repository.createSubunit(testGroupId, subunit)

            // Then
            coVerify {
                localSubunitDataSource.saveSubunit(match { it.createdBy == testUserId })
            }
        }

        @Test
        fun `preserves createdBy when not blank`() = runTest(testDispatcher) {
            // Given
            val subunit = Subunit(createdBy = "original-creator", name = "New Couple")
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs

            // When
            repository.createSubunit(testGroupId, subunit)

            // Then
            coVerify {
                localSubunitDataSource.saveSubunit(match { it.createdBy == "original-creator" })
            }
        }

        @Test
        fun `sets groupId on subunit`() = runTest(testDispatcher) {
            // Given
            val subunit = Subunit(name = "New Couple")
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs

            // When
            repository.createSubunit(testGroupId, subunit)

            // Then
            coVerify {
                localSubunitDataSource.saveSubunit(match { it.groupId == testGroupId })
            }
        }

        @Test
        fun `cloud failure does not affect local save`() = runTest(testDispatcher) {
            // Given
            val subunit = Subunit(name = "New Couple")
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs
            coEvery {
                cloudSubunitDataSource.addSubunit(any(), any())
            } throws RuntimeException("Network error")

            // When
            repository.createSubunit(testGroupId, subunit)
            advanceUntilIdle()

            // Then - Local save should still have happened
            coVerify(exactly = 1) { localSubunitDataSource.saveSubunit(any()) }
        }
    }

    @Nested
    @DisplayName("UpdateSubunit")
    inner class UpdateSubunit {

        @Test
        fun `saves to local storage first`() = runTest(testDispatcher) {
            // Given
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs

            // When
            repository.updateSubunit(testGroupId, testSubunit)

            // Then
            coVerify(exactly = 1) { localSubunitDataSource.saveSubunit(any()) }
        }

        @Test
        fun `syncs to cloud in background`() = runTest(testDispatcher) {
            // Given
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs
            coEvery { cloudSubunitDataSource.updateSubunit(any(), any()) } just Runs

            // When
            repository.updateSubunit(testGroupId, testSubunit)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) {
                cloudSubunitDataSource.updateSubunit(testGroupId, any())
            }
        }

        @Test
        fun `sets lastUpdatedAt timestamp`() = runTest(testDispatcher) {
            // Given
            val before = LocalDateTime.now()
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs

            // When
            repository.updateSubunit(testGroupId, testSubunit)

            // Then
            coVerify {
                localSubunitDataSource.saveSubunit(
                    match {
                        it.lastUpdatedAt != null && !it.lastUpdatedAt!!.isBefore(before)
                    }
                )
            }
        }

        @Test
        fun `sets groupId on updated subunit`() = runTest(testDispatcher) {
            // Given
            val subunitWithDifferentGroup = testSubunit.copy(groupId = "other-group")
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs

            // When
            repository.updateSubunit(testGroupId, subunitWithDifferentGroup)

            // Then - groupId should be overridden with the method parameter
            coVerify {
                localSubunitDataSource.saveSubunit(match { it.groupId == testGroupId })
            }
        }

        @Test
        fun `cloud failure does not affect local save`() = runTest(testDispatcher) {
            // Given
            coEvery { localSubunitDataSource.saveSubunit(any()) } just Runs
            coEvery {
                cloudSubunitDataSource.updateSubunit(any(), any())
            } throws RuntimeException("Network error")

            // When
            repository.updateSubunit(testGroupId, testSubunit)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { localSubunitDataSource.saveSubunit(any()) }
        }
    }

    @Nested
    @DisplayName("DeleteSubunit")
    inner class DeleteSubunit {

        @Test
        fun `deletes from local storage first`() = runTest(testDispatcher) {
            // Given
            val subunitId = "subunit-1"
            coEvery { localSubunitDataSource.deleteSubunit(subunitId) } just Runs

            // When
            repository.deleteSubunit(testGroupId, subunitId)

            // Then
            coVerify(exactly = 1) {
                localSubunitDataSource.deleteSubunit(subunitId)
            }
        }

        @Test
        fun `syncs deletion to cloud in background`() = runTest(testDispatcher) {
            // Given
            val subunitId = "subunit-1"
            coEvery { localSubunitDataSource.deleteSubunit(subunitId) } just Runs
            coEvery { cloudSubunitDataSource.deleteSubunit(any(), any()) } just Runs

            // When
            repository.deleteSubunit(testGroupId, subunitId)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) {
                cloudSubunitDataSource.deleteSubunit(testGroupId, subunitId)
            }
        }

        @Test
        fun `cloud deletion failure does not affect local delete`() = runTest(testDispatcher) {
            // Given
            val subunitId = "subunit-1"
            coEvery { localSubunitDataSource.deleteSubunit(subunitId) } just Runs
            coEvery {
                cloudSubunitDataSource.deleteSubunit(any(), any())
            } throws RuntimeException("Network error")

            // When
            repository.deleteSubunit(testGroupId, subunitId)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) {
                localSubunitDataSource.deleteSubunit(subunitId)
            }
        }
    }

    @Nested
    @DisplayName("GetGroupSubunitsFlow")
    inner class GetGroupSubunitsFlow {

        @Test
        fun `returns flow from local data source`() = runTest(testDispatcher) {
            // Given
            every {
                localSubunitDataSource.getSubunitsByGroupIdFlow(testGroupId)
            } returns flowOf(listOf(testSubunit))
            every {
                cloudSubunitDataSource.getSubunitsByGroupIdFlow(testGroupId)
            } returns flowOf(cloudSubunits)
            coEvery {
                localSubunitDataSource.replaceSubunitsForGroup(any(), any())
            } just Runs

            // When
            val result = repository.getGroupSubunitsFlow(testGroupId).first()

            // Then
            assertEquals(1, result.size)
            assertEquals(testSubunit.id, result[0].id)
        }

        @Test
        fun `subscribes to cloud changes on start`() = runTest(testDispatcher) {
            // Given
            every {
                localSubunitDataSource.getSubunitsByGroupIdFlow(testGroupId)
            } returns flowOf(emptyList())
            every {
                cloudSubunitDataSource.getSubunitsByGroupIdFlow(testGroupId)
            } returns flowOf(cloudSubunits)
            coEvery {
                localSubunitDataSource.replaceSubunitsForGroup(any(), any())
            } just Runs

            // When
            val flow = repository.getGroupSubunitsFlow(testGroupId)
            flow.first()
            advanceUntilIdle()

            // Then
            coVerify {
                localSubunitDataSource.replaceSubunitsForGroup(
                    testGroupId,
                    cloudSubunits
                )
            }
        }

        @Test
        fun `reuses active cloud subscription on resubscription`() = runTest(testDispatcher) {
            // Given
            every {
                localSubunitDataSource.getSubunitsByGroupIdFlow(testGroupId)
            } returns flowOf(emptyList())
            every {
                cloudSubunitDataSource.getSubunitsByGroupIdFlow(testGroupId)
            } returns flowOf(cloudSubunits)
            coEvery {
                localSubunitDataSource.replaceSubunitsForGroup(any(), any())
            } just Runs

            // When - Subscribe twice to the same groupId
            // With flowOf(), the first Job completes before the second call,
            // so a new subscription IS started (Job.isActive == false).
            val flow1 = repository.getGroupSubunitsFlow(testGroupId)
            flow1.first()
            advanceUntilIdle()

            val flow2 = repository.getGroupSubunitsFlow(testGroupId)
            flow2.first()
            advanceUntilIdle()

            // Then - replaceSubunitsForGroup should not be called more than twice
            // (once per subscription, not accumulated from leaked listeners)
            coVerify(atMost = 2) {
                localSubunitDataSource.replaceSubunitsForGroup(testGroupId, cloudSubunits)
            }
        }
    }

    @Nested
    @DisplayName("GetSubunitById")
    inner class GetSubunitById {

        @Test
        fun `returns subunit from local data source`() = runTest(testDispatcher) {
            // Given
            coEvery { localSubunitDataSource.getSubunitById("subunit-1") } returns testSubunit

            // When
            val result = repository.getSubunitById("subunit-1")

            // Then
            assertEquals(testSubunit, result)
        }

        @Test
        fun `returns null when subunit not found`() = runTest(testDispatcher) {
            // Given
            coEvery { localSubunitDataSource.getSubunitById("nonexistent") } returns null

            // When
            val result = repository.getSubunitById("nonexistent")

            // Then
            assertEquals(null, result)
        }
    }

    @Nested
    @DisplayName("GetGroupSubunits (one-shot)")
    inner class GetGroupSubunits {

        @Test
        fun `returns subunits from local data source without triggering cloud sync`() = runTest(testDispatcher) {
            // Given
            coEvery {
                localSubunitDataSource.getSubunitsByGroupId(testGroupId)
            } returns cloudSubunits

            // When
            val result = repository.getGroupSubunits(testGroupId)

            // Then
            assertEquals(cloudSubunits, result)
            coVerify(exactly = 1) { localSubunitDataSource.getSubunitsByGroupId(testGroupId) }
            // Cloud data source should NOT be touched
            coVerify(exactly = 0) { cloudSubunitDataSource.addSubunit(any(), any()) }
            coVerify(exactly = 0) { cloudSubunitDataSource.updateSubunit(any(), any()) }
            coVerify(exactly = 0) { cloudSubunitDataSource.deleteSubunit(any(), any()) }
            coVerify(exactly = 0) { cloudSubunitDataSource.fetchSubunitsByGroupId(any()) }
        }

        @Test
        fun `returns empty list when no subunits exist locally`() = runTest(testDispatcher) {
            // Given
            coEvery { localSubunitDataSource.getSubunitsByGroupId(testGroupId) } returns emptyList()

            // When
            val result = repository.getGroupSubunits(testGroupId)

            // Then
            assertTrue(result.isEmpty())
        }
    }
}
