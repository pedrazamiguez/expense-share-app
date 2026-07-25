package es.pedrazamiguez.splittrip.domain.usecase.settlement.impl

import es.pedrazamiguez.splittrip.domain.repository.SettlementNudgeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetNudgeTimestampsFlowUseCaseImplTest {

    private val repository: SettlementNudgeRepository = mockk()
    private lateinit var useCase: GetNudgeTimestampsFlowUseCaseImpl

    @BeforeEach
    fun setUp() {
        useCase = GetNudgeTimestampsFlowUseCaseImpl(repository)
    }

    @Test
    fun `invoke delegates to repository getNudgeTimestampsFlow`() = runTest {
        val expected = mapOf("settlement-1" to 1000L, "settlement-2" to 2000L)
        every { repository.getNudgeTimestampsFlow() } returns flowOf(expected)

        val result = useCase().first()

        assertEquals(expected, result)
        verify(exactly = 1) { repository.getNudgeTimestampsFlow() }
    }
}
