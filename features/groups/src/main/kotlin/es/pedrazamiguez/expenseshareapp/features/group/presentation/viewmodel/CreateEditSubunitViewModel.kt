package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.exception.ValidationException
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.SubunitShareDistributionService
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.CreateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.UpdateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.SubunitUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.CreateEditSubunitUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateEditSubunitUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateEditSubunitUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Create/Edit Sub-Unit screen.
 *
 * Loads group members and existing subunits to build the member selection list
 * with assignment awareness. Handles form state for creating or editing a subunit.
 *
 * Delegates share parsing and distribution to [SubunitShareDistributionService]
 * (pure percentage math, no amountCents coupling) and share formatting to
 * [SubunitUiMapper] (locale-aware display).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreateEditSubunitViewModel(
    private val createSubunitUseCase: CreateSubunitUseCase,
    private val updateSubunitUseCase: UpdateSubunitUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    private val subunitUiMapper: SubunitUiMapper,
    private val shareDistributionService: SubunitShareDistributionService
) : ViewModel() {

    private data class InitParams(val groupId: String, val subunitId: String?)
    private val _initParams = MutableStateFlow<InitParams?>(null)

    private val _formState = MutableStateFlow(FormState())

    // Holds the computed available members from the data layer
    private val _availableMembers = MutableStateFlow<ImmutableList<MemberUiModel>>(persistentListOf())
    private val _dataLoaded = MutableStateFlow(false)

    private val _actions = MutableSharedFlow<CreateEditSubunitUiAction>()
    val actions: SharedFlow<CreateEditSubunitUiAction> = _actions.asSharedFlow()

    val uiState: StateFlow<CreateEditSubunitUiState> = combine(
        _dataLoaded,
        _formState,
        _availableMembers
    ) { dataLoaded, form, availableMembers ->
        if (!dataLoaded) {
            CreateEditSubunitUiState()
        } else {
            val params = _initParams.value
            CreateEditSubunitUiState(
                isLoading = false,
                isSaving = form.isSaving,
                isEditing = params?.subunitId != null,
                name = form.name,
                selectedMemberIds = form.selectedMemberIds.toImmutableList(),
                memberShares = form.memberShares,
                lockedMemberIds = form.lockedMemberIds.toImmutableSet(),
                availableMembers = availableMembers,
                nameError = form.nameError,
                membersError = form.membersError,
                sharesError = form.sharesError
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConstants.FLOW_RETENTION_TIME),
        initialValue = CreateEditSubunitUiState()
    )

    init {
        // Observe init params and load data reactively
        viewModelScope.launch {
            _initParams
                .filter { it != null }
                .flatMapLatest { params ->
                    val groupId = params!!.groupId
                    val subunitId = params.subunitId

                    getGroupSubunitsFlowUseCase(groupId)
                        .map { subunits ->
                            val group = getGroupByIdUseCase(groupId)
                            val memberIds = group?.members ?: emptyList()
                            val memberProfiles = getMemberProfilesUseCase(memberIds)

                            // Pre-fill form for edit mode (only on first load)
                            val editingSubunit = subunitId?.let { id -> subunits.find { it.id == id } }
                            if (editingSubunit != null && !_formState.value.initialized) {
                                _formState.update {
                                    FormState(
                                        initialized = true,
                                        name = editingSubunit.name,
                                        selectedMemberIds = editingSubunit.memberIds,
                                        memberShares = editingSubunit.memberShares.mapValues { (_, share) ->
                                            subunitUiMapper.formatShareAsPercentage(share)
                                        }
                                    )
                                }
                            } else if (!_formState.value.initialized) {
                                _formState.update { it.copy(initialized = true) }
                            }

                            subunitUiMapper.toMemberUiModelList(
                                memberIds = memberIds,
                                memberProfiles = memberProfiles,
                                subunits = subunits,
                                excludeSubunitId = subunitId
                            )
                        }
                }
                .collect { availableMembers ->
                    _availableMembers.value = availableMembers
                    _dataLoaded.value = true
                }
        }
    }

    fun init(groupId: String, subunitId: String?) {
        val newParams = InitParams(groupId, subunitId)
        if (newParams != _initParams.value) {
            _initParams.value = newParams
        }
    }

    fun onEvent(event: CreateEditSubunitUiEvent) {
        when (event) {
            is CreateEditSubunitUiEvent.UpdateName -> updateName(event.name)
            is CreateEditSubunitUiEvent.ToggleMember -> toggleMember(event.userId)
            is CreateEditSubunitUiEvent.UpdateMemberShare -> updateMemberShare(event.userId, event.share)
            is CreateEditSubunitUiEvent.ToggleShareLock -> toggleShareLock(event.userId)
            CreateEditSubunitUiEvent.Save -> save()
        }
    }

    private fun updateName(name: String) {
        _formState.update { it.copy(name = name, nameError = null) }
    }

    private fun toggleMember(userId: String) {
        _formState.update { form ->
            val currentIds = form.selectedMemberIds
            val updatedIds = if (userId in currentIds) {
                currentIds - userId
            } else {
                currentIds + userId
            }

            // Distribute shares evenly among all selected members
            val evenShares = shareDistributionService.distributeEvenly(updatedIds)
            val updatedShares = evenShares.mapValues { (_, share) ->
                subunitUiMapper.formatShareAsPercentage(share)
            }

            form.copy(
                selectedMemberIds = updatedIds,
                memberShares = updatedShares,
                lockedMemberIds = emptySet(),
                membersError = null
            )
        }
    }

    private fun updateMemberShare(userId: String, share: String) {
        _formState.update { form ->
            val updatedShares = form.memberShares.toMutableMap()
            updatedShares[userId] = share

            // Auto-lock the edited member
            val updatedLocks = form.lockedMemberIds + userId

            // Parse the typed value (locale-safe) and redistribute remaining to other selected members
            val normalized = CurrencyConverter.normalizeAmountString(share)
            val parsedValue = normalized.toBigDecimalOrNull()
                ?.divide(java.math.BigDecimal("100"), 10, java.math.RoundingMode.HALF_UP)
            if (parsedValue != null) {
                // Other locked members (excluding the currently edited one)
                val otherLockedIds = (updatedLocks - userId).filter { it in form.selectedMemberIds }

                // Build locked shares map — unparseable locked shares are treated as 0 budget
                // but still excluded from redistribution (they keep their displayed text)
                val lockedSharesMap = otherLockedIds.associate { lockedId ->
                    val lockedText = updatedShares[lockedId] ?: ""
                    val lockedNorm = CurrencyConverter.normalizeAmountString(lockedText)
                    val lockedVal = lockedNorm.toBigDecimalOrNull()
                        ?.divide(java.math.BigDecimal("100"), 10, java.math.RoundingMode.HALF_UP)
                        ?: java.math.BigDecimal.ZERO
                    lockedId to lockedVal
                }

                // Only redistribute to non-locked selected members
                val unlockedOtherIds = form.selectedMemberIds.filter { it != userId && it !in otherLockedIds }

                val redistribution = shareDistributionService.redistributeRemaining(
                    editedShare = parsedValue,
                    otherMemberIds = unlockedOtherIds,
                    lockedShares = lockedSharesMap
                )
                redistribution.forEach { (otherId, otherShare) ->
                    updatedShares[otherId] = subunitUiMapper.formatShareAsPercentage(otherShare)
                }
            }

            form.copy(
                memberShares = updatedShares,
                lockedMemberIds = updatedLocks,
                sharesError = null
            )
        }
    }

    private fun toggleShareLock(userId: String) {
        _formState.update { form ->
            val updatedLocks = if (userId in form.lockedMemberIds) {
                form.lockedMemberIds - userId
            } else {
                form.lockedMemberIds + userId
            }
            form.copy(lockedMemberIds = updatedLocks)
        }
    }

    private fun save() {
        val form = _formState.value
        val params = _initParams.value ?: return

        // Client-side validation for immediate feedback
        if (form.name.isBlank()) {
            _formState.update {
                it.copy(nameError = UiText.StringResource(R.string.subunit_error_name_empty))
            }
            return
        }
        if (form.selectedMemberIds.isEmpty()) {
            _formState.update {
                it.copy(membersError = UiText.StringResource(R.string.subunit_error_no_members))
            }
            return
        }

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val memberShares = shareDistributionService.parseShareTexts(
                selectedMemberIds = form.selectedMemberIds,
                memberShareTexts = form.memberShares
            )

            // If the form has non-blank share entries but parsing returned empty,
            // the input was unparseable — surface an error instead of auto-normalizing.
            val hasNonBlankShares = form.memberShares.values.any { it.isNotBlank() }
            if (memberShares.isEmpty() && hasNonBlankShares) {
                _formState.update {
                    it.copy(
                        isSaving = false,
                        sharesError = UiText.StringResource(R.string.subunit_error_validation_failed)
                    )
                }
                return@launch
            }

            val subunit = Subunit(
                id = params.subunitId ?: "",
                groupId = params.groupId,
                name = form.name.trim(),
                memberIds = form.selectedMemberIds,
                memberShares = memberShares
            )

            val result = if (params.subunitId != null) {
                updateSubunitUseCase(params.groupId, subunit)
                    .map { UiText.StringResource(R.string.subunit_updated_success) }
            } else {
                createSubunitUseCase(params.groupId, subunit)
                    .map { UiText.StringResource(R.string.subunit_created_success) }
            }

            result
                .onSuccess { message ->
                    _actions.emit(CreateEditSubunitUiAction.ShowSuccess(message))
                    _actions.emit(CreateEditSubunitUiAction.NavigateBack)
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to save subunit")
                    _formState.update { it.copy(isSaving = false) }

                    val errorMessage = if (error is ValidationException) {
                        UiText.StringResource(R.string.subunit_error_validation_failed)
                    } else {
                        UiText.StringResource(R.string.subunit_error_save_failed)
                    }

                    _actions.emit(CreateEditSubunitUiAction.ShowError(errorMessage))
                }
        }
    }

    private data class FormState(
        val initialized: Boolean = false,
        val isSaving: Boolean = false,
        val name: String = "",
        val selectedMemberIds: List<String> = emptyList(),
        val memberShares: Map<String, String> = emptyMap(),
        val lockedMemberIds: Set<String> = emptySet(),
        val nameError: UiText? = null,
        val membersError: UiText? = null,
        val sharesError: UiText? = null
    )
}
