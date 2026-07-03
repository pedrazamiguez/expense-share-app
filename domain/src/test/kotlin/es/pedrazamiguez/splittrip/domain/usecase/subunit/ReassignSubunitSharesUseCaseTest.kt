package es.pedrazamiguez.splittrip.domain.usecase.subunit

import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.SubunitShareDistributionService
import es.pedrazamiguez.splittrip.domain.usecase.subunit.impl.ReassignSubunitSharesUseCaseImpl
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ReassignSubunitSharesUseCase")
class ReassignSubunitSharesUseCaseTest {

    private lateinit var subunitRepository: SubunitRepository
    private lateinit var subunitShareDistributionService: SubunitShareDistributionService
    private lateinit var useCase: ReassignSubunitSharesUseCase

    private val groupId = "group-123"
    private val leavingUserId = "user-A"
    private val memberB = "user-B"
    private val memberC = "user-C"

    @BeforeEach
    fun setUp() {
        subunitRepository = mockk()
        subunitShareDistributionService = mockk()
        useCase = ReassignSubunitSharesUseCaseImpl(
            subunitRepository = subunitRepository,
            subunitShareDistributionService = subunitShareDistributionService
        )
        coEvery { subunitRepository.updateSubunit(any(), any()) } just Runs
    }

    @Nested
    @DisplayName("MemberInNoSubunits")
    inner class MemberInNoSubunits {

        @Test
        fun `does not call updateSubunit when member is in no subunits`() = runTest {
            coEvery { subunitRepository.getGroupSubunits(groupId) } returns emptyList()

            val result = useCase(groupId, leavingUserId)

            assertTrue(result.isSuccess)
            coVerify(exactly = 0) { subunitRepository.updateSubunit(any(), any()) }
        }
    }

    @Nested
    @DisplayName("MemberWithExplicitShares")
    inner class MemberWithExplicitShares {

        @Nested
        @DisplayName("TwoMembers")
        inner class TwoMembers {

            @Test
            fun `removes leaving member and gives remaining member 100 percent`() = runTest {
                val subunit = Subunit(
                    id = "subunit-1",
                    memberIds = listOf(leavingUserId, memberB),
                    memberShares = mapOf(
                        leavingUserId to BigDecimal("0.5"),
                        memberB to BigDecimal("0.5")
                    )
                )
                coEvery { subunitRepository.getGroupSubunits(groupId) } returns listOf(subunit)
                coEvery {
                    subunitShareDistributionService.rescaleSharesAfterRemoval(
                        removedMemberId = leavingUserId,
                        currentShares = subunit.memberShares
                    )
                } returns mapOf(memberB to BigDecimal.ONE)

                val result = useCase(groupId, leavingUserId)

                assertTrue(result.isSuccess)
                coVerify(exactly = 1) {
                    subunitRepository.updateSubunit(
                        groupId = groupId,
                        subunit = withArg { updated ->
                            assertTrue(updated.memberIds == listOf(memberB))
                            assertTrue(updated.memberShares == mapOf(memberB to BigDecimal.ONE))
                        }
                    )
                }
            }
        }

        @Nested
        @DisplayName("ThreeMembers")
        inner class ThreeMembers {

            @Test
            fun `redistributes proportionally among remaining two members`() = runTest {
                val subunit = Subunit(
                    id = "subunit-1",
                    memberIds = listOf(leavingUserId, memberB, memberC),
                    memberShares = mapOf(
                        leavingUserId to BigDecimal("0.6"),
                        memberB to BigDecimal("0.3"),
                        memberC to BigDecimal("0.1")
                    )
                )
                coEvery { subunitRepository.getGroupSubunits(groupId) } returns listOf(subunit)
                coEvery {
                    subunitShareDistributionService.rescaleSharesAfterRemoval(
                        removedMemberId = leavingUserId,
                        currentShares = subunit.memberShares
                    )
                } returns mapOf(
                    memberB to BigDecimal("0.75"),
                    memberC to BigDecimal("0.25")
                )

                val result = useCase(groupId, leavingUserId)

                assertTrue(result.isSuccess)
                coVerify(exactly = 1) {
                    subunitRepository.updateSubunit(
                        groupId = groupId,
                        subunit = withArg { updated ->
                            assertTrue(
                                updated.memberShares == mapOf(
                                    memberB to BigDecimal("0.75"),
                                    memberC to BigDecimal("0.25")
                                )
                            )
                        }
                    )
                }
            }
        }

        @Nested
        @DisplayName("MultipleSubunits")
        inner class MultipleSubunits {

            @Test
            fun `updates all subunits the member belongs to`() = runTest {
                val subunit1 = Subunit(
                    id = "subunit-1",
                    memberIds = listOf(leavingUserId, memberB),
                    memberShares = mapOf(
                        leavingUserId to BigDecimal("0.5"),
                        memberB to BigDecimal("0.5")
                    )
                )
                val subunit2 = Subunit(
                    id = "subunit-2",
                    memberIds = listOf(leavingUserId, memberC),
                    memberShares = mapOf(
                        leavingUserId to BigDecimal("0.6"),
                        memberC to BigDecimal("0.4")
                    )
                )
                coEvery { subunitRepository.getGroupSubunits(groupId) } returns listOf(subunit1, subunit2)
                coEvery {
                    subunitShareDistributionService.rescaleSharesAfterRemoval(
                        removedMemberId = leavingUserId,
                        currentShares = any()
                    )
                } returns mapOf(memberB to BigDecimal.ONE) andThen mapOf(memberC to BigDecimal.ONE)

                val result = useCase(groupId, leavingUserId)

                assertTrue(result.isSuccess)
                coVerify(exactly = 2) { subunitRepository.updateSubunit(any(), any()) }
            }
        }

        @Nested
        @DisplayName("SoloMember")
        inner class SoloMember {

            @Test
            fun `preserves subunit as empty shell when leaving member was the only member`() = runTest {
                val subunit = Subunit(
                    id = "subunit-1",
                    memberIds = listOf(leavingUserId),
                    memberShares = mapOf(leavingUserId to BigDecimal.ONE)
                )
                coEvery { subunitRepository.getGroupSubunits(groupId) } returns listOf(subunit)
                coEvery {
                    subunitShareDistributionService.rescaleSharesAfterRemoval(
                        removedMemberId = leavingUserId,
                        currentShares = subunit.memberShares
                    )
                } returns emptyMap()

                val result = useCase(groupId, leavingUserId)

                assertTrue(result.isSuccess)
                coVerify(exactly = 1) {
                    subunitRepository.updateSubunit(
                        groupId = groupId,
                        subunit = withArg { updated ->
                            assertTrue(updated.memberIds.isEmpty())
                            assertTrue(updated.memberShares.isEmpty())
                        }
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("MemberWithImpliedEqualShares")
    inner class MemberWithImpliedEqualShares {

        @Test
        fun `removes member from memberIds when memberShares is empty`() = runTest {
            val subunit = Subunit(
                id = "subunit-1",
                memberIds = listOf(leavingUserId, memberB),
                memberShares = emptyMap()
            )
            coEvery { subunitRepository.getGroupSubunits(groupId) } returns listOf(subunit)

            val result = useCase(groupId, leavingUserId)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                subunitRepository.updateSubunit(
                    groupId = groupId,
                    subunit = withArg { updated ->
                        assertTrue(updated.memberIds == listOf(memberB))
                        assertTrue(updated.memberShares.isEmpty())
                    }
                )
            }
        }
    }

    @Nested
    @DisplayName("MemberInSomeSubunitsButNotAll")
    inner class MemberInSomeSubunitsButNotAll {

        @Test
        fun `skips subunits where the leaving member is not present`() = runTest {
            val subunitWithLeaving = Subunit(
                id = "subunit-1",
                memberIds = listOf(leavingUserId, memberB),
                memberShares = mapOf(
                    leavingUserId to BigDecimal("0.5"),
                    memberB to BigDecimal("0.5")
                )
            )
            val subunitWithoutLeaving = Subunit(
                id = "subunit-2",
                memberIds = listOf(memberB, memberC),
                memberShares = mapOf(
                    memberB to BigDecimal("0.6"),
                    memberC to BigDecimal("0.4")
                )
            )
            coEvery { subunitRepository.getGroupSubunits(groupId) } returns
                listOf(subunitWithLeaving, subunitWithoutLeaving)
            coEvery {
                subunitShareDistributionService.rescaleSharesAfterRemoval(
                    removedMemberId = leavingUserId,
                    currentShares = subunitWithLeaving.memberShares
                )
            } returns mapOf(memberB to BigDecimal.ONE)

            val result = useCase(groupId, leavingUserId)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { subunitRepository.updateSubunit(any(), any()) }
        }
    }
}
