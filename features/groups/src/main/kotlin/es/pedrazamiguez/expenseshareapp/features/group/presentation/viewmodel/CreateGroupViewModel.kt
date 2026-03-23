package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.service.EmailValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetSupportedCurrenciesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.SearchUsersByEmailUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.CreateGroupUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateGroupViewModel(
    private val createGroupUseCase: CreateGroupUseCase,
    private val getSupportedCurrenciesUseCase: GetSupportedCurrenciesUseCase,
    private val getUserDefaultCurrencyUseCase: GetUserDefaultCurrencyUseCase,
    private val searchUsersByEmailUseCase: SearchUsersByEmailUseCase,
    private val emailValidationService: EmailValidationService,
    private val groupUiMapper: GroupUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<CreateGroupUiAction>()
    val actions: SharedFlow<CreateGroupUiAction> = _actions.asSharedFlow()

    private var memberSearchJob: Job? = null

    fun onEvent(event: CreateGroupUiEvent, onCreateGroupSuccess: () -> Unit) {
        Timber.i("Event: $event")
        when (event) {
            is CreateGroupUiEvent.LoadCurrencies -> loadCurrencies()
            is CreateGroupUiEvent.NameChanged -> handleNameChanged(event.name)
            is CreateGroupUiEvent.DescriptionChanged -> handleDescriptionChanged(event.description)
            is CreateGroupUiEvent.CurrencySelected -> handleCurrencySelected(event.code)
            is CreateGroupUiEvent.ExtraCurrencyToggled -> handleExtraCurrencyToggled(event.code)
            is CreateGroupUiEvent.MemberSearchQueryChanged -> searchMembers(event.query)
            is CreateGroupUiEvent.MemberSelected -> handleMemberSelected(event)
            is CreateGroupUiEvent.MemberRemoved -> handleMemberRemoved(event)
            is CreateGroupUiEvent.SubmitCreateGroup -> handleSubmit(onCreateGroupSuccess)
            is CreateGroupUiEvent.NextStep -> handleNextStep()
            is CreateGroupUiEvent.PreviousStep -> handlePreviousStep()
        }
    }

    private fun handleNextStep() {
        val state = _uiState.value
        val nextIndex = state.currentStepIndex + 1
        val nextStep = state.steps.getOrNull(nextIndex) ?: return
        _uiState.update { it.copy(currentStep = nextStep, error = null) }
    }

    private fun handlePreviousStep() {
        val state = _uiState.value
        val prevIndex = state.currentStepIndex - 1
        val prevStep = state.steps.getOrNull(prevIndex)
        if (prevStep != null) {
            _uiState.update { it.copy(currentStep = prevStep, error = null) }
        } else {
            viewModelScope.launch { _actions.emit(CreateGroupUiAction.NavigateBack) }
        }
    }

    private fun handleNameChanged(name: String) {
        _uiState.update { it.copy(groupName = name, isNameValid = name.isNotBlank()) }
    }

    private fun handleDescriptionChanged(description: String) {
        _uiState.update { it.copy(groupDescription = description) }
    }

    private fun handleCurrencySelected(code: String) {
        _uiState.update { state ->
            val selected = state.availableCurrencies.find { it.code == code } ?: return
            val updatedExtras = state.extraCurrencies
                .filter { it.code != code }
                .toImmutableList()
            state.copy(selectedCurrency = selected, extraCurrencies = updatedExtras)
        }
    }

    private fun handleExtraCurrencyToggled(code: String) {
        _uiState.update { state ->
            val currentExtras = state.extraCurrencies
            val updatedExtras = if (currentExtras.any { it.code == code }) {
                currentExtras.filter { it.code != code }
            } else {
                val item = state.availableCurrencies.find { it.code == code } ?: return
                currentExtras + item
            }.toImmutableList()
            state.copy(extraCurrencies = updatedExtras)
        }
    }

    private fun handleMemberSelected(event: CreateGroupUiEvent.MemberSelected) {
        _uiState.update { state ->
            if (state.selectedMembers.any { it.userId == event.user.userId }) {
                state
            } else {
                state.copy(
                    selectedMembers = (state.selectedMembers + event.user).toImmutableList(),
                    memberSearchResults = persistentListOf()
                )
            }
        }
    }

    private fun handleMemberRemoved(event: CreateGroupUiEvent.MemberRemoved) {
        _uiState.update { state ->
            state.copy(
                selectedMembers = state.selectedMembers
                    .filter { it.userId != event.user.userId }
                    .toImmutableList()
            )
        }
    }

    private fun handleSubmit(onCreateGroupSuccess: () -> Unit) {
        if (_uiState.value.groupName.isBlank()) {
            _uiState.update {
                it.copy(isNameValid = false, error = UiText.StringResource(R.string.group_error_name_empty))
            }
            return
        }
        createGroup(onCreateGroupSuccess)
    }

    private fun searchMembers(query: String) {
        memberSearchJob?.cancel()

        if (query.length < MEMBER_SEARCH_MIN_QUERY_LENGTH || !emailValidationService.isValidEmail(query)) {
            _uiState.update { it.copy(memberSearchResults = persistentListOf(), isSearchingMembers = false) }
            return
        }

        memberSearchJob = viewModelScope.launch {
            delay(MEMBER_SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(isSearchingMembers = true) }

            searchUsersByEmailUseCase(query).onSuccess { users ->
                val selectedIds = _uiState.value.selectedMembers.map { it.userId }.toSet()
                _uiState.update {
                    it.copy(
                        memberSearchResults = users.filter { u -> u.userId !in selectedIds }.toImmutableList(),
                        isSearchingMembers = false
                    )
                }
            }.onFailure { e ->
                Timber.e(e, "Failed to search users by email")
                _uiState.update { it.copy(memberSearchResults = persistentListOf(), isSearchingMembers = false) }
            }
        }
    }

    private fun loadCurrencies() {
        if (_uiState.value.availableCurrencies.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCurrencies = true) }

            val userDefaultCurrency =
                getUserDefaultCurrencyUseCase().firstOrNull() ?: AppConstants.DEFAULT_CURRENCY_CODE

            getSupportedCurrenciesUseCase().onSuccess { sortedCurrencies ->
                val mappedCurrencies = groupUiMapper.toCurrencyUiModels(sortedCurrencies)
                val defaultCurrency = mappedCurrencies.find { it.code == userDefaultCurrency }
                    ?: mappedCurrencies.firstOrNull()

                _uiState.update {
                    it.copy(
                        availableCurrencies = mappedCurrencies,
                        selectedCurrency = it.selectedCurrency ?: defaultCurrency,
                        isLoadingCurrencies = false
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoadingCurrencies = false,
                        error = UiText.StringResource(R.string.group_error_load_currencies)
                    )
                }
                Timber.e(e, "Failed to load currencies")
            }
        }
    }

    private fun createGroup(onCreateGroupSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            val groupName = state.groupName

            createGroupUseCase(
                Group(
                    name = groupName,
                    description = state.groupDescription,
                    currency = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE,
                    extraCurrencies = state.extraCurrencies.map { it.code },
                    members = state.selectedMembers.map { it.userId }
                )
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _actions.emit(
                    CreateGroupUiAction.ShowSuccess(UiText.StringResource(R.string.group_created_success, groupName))
                )
                onCreateGroupSuccess()
            }.onFailure { e ->
                Timber.e(e, "Failed to create group")
                _uiState.update {
                    it.copy(isLoading = false, error = UiText.StringResource(R.string.group_error_creation_failed))
                }
                _actions.emit(
                    CreateGroupUiAction.ShowError(UiText.StringResource(R.string.group_error_creation_failed))
                )
            }
        }
    }

    companion object {
        private const val MEMBER_SEARCH_DEBOUNCE_MS = 300L
        private const val MEMBER_SEARCH_MIN_QUERY_LENGTH = 3
    }
}
