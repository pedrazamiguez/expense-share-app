# Architecture Diagrams

Visual guide to the ExpenseShareApp architecture using Mermaid diagrams. Each section covers a key aspect of the system — from high-level module structure down to individual data flows and patterns.

---

## Table of Contents

1. [Module Dependency Graph](#1-module-dependency-graph)
2. [Clean Architecture Layers](#2-clean-architecture-layers)
3. [Feature Module Anatomy](#3-feature-module-anatomy)
4. [DI Wiring (Koin)](#4-di-wiring-koin)
5. [MVI State Management](#5-mvi-state-management)
6. [Feature vs Screen Pattern](#6-feature-vs-screen-pattern)
7. [Navigation Architecture](#7-navigation-architecture)
8. [Offline-First Data Flow — Reads](#8-offline-first-data-flow--reads)
9. [Offline-First Data Flow — Writes](#9-offline-first-data-flow--writes)
10. [Real-Time Multi-Device Sync](#10-real-time-multi-device-sync)
11. [Data Mapping Pipeline](#11-data-mapping-pipeline)
12. [Compose Preview System](#12-compose-preview-system)
13. [Coroutine & Flow Architecture](#13-coroutine--flow-architecture)

---

## 1. Module Dependency Graph

How Gradle modules depend on each other. The `:app` module is the only one that sees everything — it wires DI. Features are fully isolated from each other and from the data layer.

```mermaid
graph TD
    APP[":app<br/><i>DI wiring only</i>"]

    subgraph Core["Core Modules"]
        COMMON[":core:common<br/><i>Constants, UiText,<br/>Providers</i>"]
        DS[":core:design-system<br/><i>UI components, Routes,<br/>NavigationProvider,<br/>TabGraphContributor,<br/>ScreenUiProvider</i>"]
    end

    subgraph Domain["Domain Module"]
        DOM[":domain<br/><i>Models, Repository interfaces,<br/>UseCases, Services</i>"]
    end

    subgraph Data["Data Modules"]
        DATA[":data<br/><i>Repository implementations</i>"]
        LOCAL[":data:local<br/><i>Room DAOs, Entities,<br/>DataStore</i>"]
        FIREBASE[":data:firebase<br/><i>Firestore, Auth</i>"]
        REMOTE[":data:remote<br/><i>Retrofit (Currency API)</i>"]
    end

    subgraph Features["Feature Modules"]
        FG[":features:groups"]
        FE[":features:expenses"]
        FB[":features:balances"]
        FC[":features:contributions"]
        FW[":features:withdrawals"]
        FSU[":features:subunits"]
        FP[":features:profile"]
        FS[":features:settings"]
        FA[":features:authentication"]
        FO[":features:onboarding"]
        FM[":features:main-entry"]
        FL[":features:activity-logging"]
    end

    APP --> Core
    APP --> Domain
    APP --> Data
    APP --> Features

    DS --> COMMON

    DATA --> DOM
    DATA --> LOCAL
    DATA --> FIREBASE
    DATA --> REMOTE
    LOCAL --> DOM
    FIREBASE --> DOM
    REMOTE --> DOM

    FG --> DOM
    FG --> Core
    FE --> DOM
    FE --> Core
    FB --> DOM
    FB --> Core
    FC --> DOM
    FC --> Core
    FW --> DOM
    FW --> Core
    FSU --> DOM
    FSU --> Core
    FP --> DOM
    FP --> Core
    FS --> DOM
    FS --> Core
    FA --> DOM
    FA --> Core
    FO --> Core
    FM --> Core

    style APP fill:#4a9eff,color:#fff
    style DOM fill:#22c55e,color:#fff
    style DATA fill:#f59e0b,color:#fff
    style LOCAL fill:#f59e0b,color:#fff
    style FIREBASE fill:#f59e0b,color:#fff
    style REMOTE fill:#f59e0b,color:#fff
    style COMMON fill:#8b5cf6,color:#fff
    style DS fill:#8b5cf6,color:#fff
```

### Visibility Rules

```mermaid
graph LR
    subgraph Forbidden["❌ Forbidden Dependencies"]
        direction LR
        F1[":features:groups"] -. "CANNOT see" .-> F2[":features:expenses"]
        F3[":features:balances"] -. "CANNOT see" .-> DATA2[":data"]
        F4[":features:profile"] -. "CANNOT see" .-> LOCAL2[":data:local"]
    end

    subgraph Allowed["✅ Allowed Dependencies"]
        direction LR
        F5[":features:groups"] --> DOM2[":domain"]
        F6[":features:expenses"] --> CORE2[":core:*"]
        DATA3[":data"] --> DOM3[":domain"]
    end

    style F1 fill:#fee2e2,color:#991b1b
    style F2 fill:#fee2e2,color:#991b1b
    style F3 fill:#fee2e2,color:#991b1b
    style F4 fill:#fee2e2,color:#991b1b
    style DATA2 fill:#fee2e2,color:#991b1b
    style LOCAL2 fill:#fee2e2,color:#991b1b
    style F5 fill:#dcfce7,color:#166534
    style F6 fill:#dcfce7,color:#166534
    style DATA3 fill:#dcfce7,color:#166534
    style DOM2 fill:#dcfce7,color:#166534
    style DOM3 fill:#dcfce7,color:#166534
    style CORE2 fill:#dcfce7,color:#166534
```

---

## 2. Clean Architecture Layers

The three concentric rings of the architecture, showing dependency direction (always inward).

```mermaid
graph TB
    subgraph Presentation["🎨 Presentation Layer (features:*)"]
        direction TB
        VM["ViewModels<br/><i>State orchestration</i>"]
        MAPPER["UiMappers<br/><i>Domain → UiModel</i>"]
        SCREEN["Screens<br/><i>Stateless Composables</i>"]
        FEATURE["Features<br/><i>Orchestrator Composables</i>"]
    end

    subgraph DomainLayer["🧠 Domain Layer (:domain)"]
        direction TB
        UC["UseCases<br/><i>Single responsibility actions</i>"]
        SVC["Domain Services<br/><i>Business rules & validation</i>"]
        REPO_IF["Repository Interfaces<br/><i>Contracts only</i>"]
        MODEL["Domain Models<br/><i>Expense, Group, User...</i>"]
    end

    subgraph DataLayer["💾 Data Layer (:data:*)"]
        direction TB
        REPO_IMPL["Repository Implementations<br/><i>Offline-first logic</i>"]
        LOCAL_DS["Local DataSource<br/><i>Room DAOs</i>"]
        CLOUD_DS["Cloud DataSource<br/><i>Firestore</i>"]
        REMOTE_DS["Remote DataSource<br/><i>Retrofit</i>"]
    end

    VM --> UC
    VM --> MAPPER
    VM --> SVC
    FEATURE --> VM
    FEATURE --> SCREEN

    UC --> REPO_IF
    UC --> SVC

    REPO_IMPL -.-> REPO_IF
    REPO_IMPL --> LOCAL_DS
    REPO_IMPL --> CLOUD_DS
    REPO_IMPL --> REMOTE_DS

    style Presentation fill:#eff6ff,stroke:#3b82f6
    style DomainLayer fill:#f0fdf4,stroke:#22c55e
    style DataLayer fill:#fffbeb,stroke:#f59e0b
```

### What Belongs Where

```mermaid
graph LR
    subgraph Domain["Domain Layer"]
        D1["Models: Expense, Group, User"]
        D2["UseCases: AddExpenseUseCase"]
        D3["Services: ExpenseCalculatorService,<br/>ExpenseValidationService"]
        D4["Interfaces: ExpenseRepository"]
        D5["Enums: PaymentStatus, SplitType"]
    end

    subgraph DataImpl["Data Layer"]
        DA1["Impls: ExpenseRepositoryImpl"]
        DA2["Entities: ExpenseEntity"]
        DA3["DAOs: ExpenseDao"]
        DA4["Mappers: ExpenseDocumentMapper"]
        DA5["DataStore: UserPreferences"]
    end

    subgraph PresentationImpl["Presentation Layer"]
        P1["ViewModels: ExpensesViewModel"]
        P2["UiMappers: ExpenseUiMapper"]
        P3["UiModels: ExpenseUiModel"]
        P4["UiState / UiEvent / UiAction"]
        P5["Feature + Screen Composables"]
    end

    style Domain fill:#f0fdf4
    style DataImpl fill:#fffbeb
    style PresentationImpl fill:#eff6ff
```

---

## 3. Feature Module Anatomy

Internal structure of a typical feature module (e.g., `:features:groups`).

```mermaid
graph TB
    subgraph FeatureModule[":features:groups"]
        subgraph DI["di/"]
            UIMOD["GroupsUiModule.kt<br/><i>ViewModel, Mapper,<br/>NavigationProvider,<br/>ScreenUiProvider</i>"]
        end

        subgraph Navigation["navigation/"]
            NAV["GroupsNavigation.kt<br/><i>NavGraphBuilder extension</i>"]
            NAVIMPL["impl/GroupsNavigationProviderImpl.kt<br/><i>Tab icon, label, order</i>"]
        end

        subgraph Presentation["presentation/"]
            subgraph Feature["feature/"]
                FEAT["GroupsFeature.kt<br/><i>Orchestrator</i>"]
            end
            subgraph Screen["screen/"]
                SCR["GroupsScreen.kt<br/><i>Stateless renderer</i>"]
                SUIPROV["impl/GroupsScreenUiProviderImpl.kt<br/><i>TopBar + FAB definition</i>"]
            end
            subgraph ViewModel["viewmodel/"]
                VMFILE["GroupsViewModel.kt"]
                STATE["state/GroupsUiState.kt"]
                EVENT["event/GroupsUiEvent.kt"]
                ACTION["action/GroupsUiAction.kt"]
            end
            subgraph MapperPkg["mapper/"]
                MAPIF["GroupUiMapper.kt<br/><i>interface</i>"]
                MAPIMPL["impl/GroupUiMapperImpl.kt"]
            end
            subgraph Model["model/"]
                UIMODEL["GroupUiModel.kt"]
            end
            subgraph Component["component/"]
                COMP["GroupCard.kt, etc."]
            end
        end

        subgraph Debug["src/debug/"]
            PREVIEW["preview/GroupUiPreviewHelper.kt"]
        end
    end

    FEAT --> VMFILE
    FEAT --> SCR
    VMFILE --> STATE
    VMFILE --> EVENT
    VMFILE --> ACTION
    VMFILE --> MAPIF
    MAPIMPL -.-> MAPIF
    SCR --> STATE
    SCR --> EVENT
    SCR --> COMP

    style FeatureModule fill:#f8fafc,stroke:#64748b
    style DI fill:#dbeafe
    style Navigation fill:#fef3c7
    style Presentation fill:#f0fdf4
    style Debug fill:#fce7f3
```

---

## 4. DI Wiring (Koin)

How Koin modules are organized in a triple pattern and aggregated in the `:app` module.

```mermaid
graph TB
    subgraph AppModule["app/.../di/"]
        AGG["FeatureModuleAggregations.kt"]
        DMOD["DataModuleAggregations.kt"]
        AMOD["AppModule.kt"]
    end

    subgraph GroupsTriple["Groups Feature Triple"]
        GD["groupsDomainModule<br/><i>:domain — UseCases</i>"]
        GDA["groupsDataModule<br/><i>:data — Repository impl</i>"]
        GU["groupsUiModule<br/><i>:features:groups — ViewModel,<br/>Mapper, NavProvider,<br/>ScreenUiProvider</i>"]
    end

    subgraph ExpensesTriple["Expenses Feature Triple"]
        ED["expensesDomainModule"]
        EDA["expensesDataModule"]
        EU["expensesUiModule"]
    end

    subgraph SubunitsTriple["Subunits Feature Triple"]
        SUD["subunitsDomainModule"]
        SUDA["subunitsDataModule"]
        SUU["subunitsUiModule<br/><i>:features:subunits — ViewModel,<br/>Mapper, TabGraphContributor</i>"]
    end

    subgraph ContributionsDouble["Contributions Feature (no data module)"]
        CD["contributionsDomainModule"]
        CU["contributionsUiModule<br/><i>:features:contributions — ViewModel,<br/>Mapper, TabGraphContributor</i>"]
    end

    subgraph WithdrawalsDouble["Withdrawals Feature (no data module)"]
        WD["withdrawalsDomainModule"]
        WU["withdrawalsUiModule<br/><i>:features:withdrawals — ViewModel,<br/>Mapper, TabGraphContributor</i>"]
    end

    subgraph DataInfra["Data Infrastructure"]
        DL["dataLocalModule<br/><i>Room DB, DAOs</i>"]
        DF["dataFirebaseModule<br/><i>Firestore, Auth</i>"]
        DR["dataRemoteModule<br/><i>Retrofit</i>"]
        DC["dataCommonModule"]
    end

    subgraph AppProviders["App Providers"]
        LP["LocaleProvider"]
        RP["ResourceProvider"]
        AMP["AppMetadataProvider"]
        IP["IntentProvider"]
    end

    AGG --> GD & GDA & GU
    AGG --> ED & EDA & EU
    AGG --> SUD & SUDA & SUU
    AGG --> CD & CU
    AGG --> WD & WU
    DMOD --> DL & DF & DR & DC
    AMOD --> LP & RP & AMP & IP

    style AppModule fill:#4a9eff,color:#fff
    style GroupsTriple fill:#f0fdf4,stroke:#22c55e
    style ExpensesTriple fill:#f0fdf4,stroke:#22c55e
    style SubunitsTriple fill:#f0fdf4,stroke:#22c55e
    style ContributionsDouble fill:#eff6ff,stroke:#3b82f6
    style WithdrawalsDouble fill:#eff6ff,stroke:#3b82f6
    style DataInfra fill:#fffbeb,stroke:#f59e0b
    style AppProviders fill:#f3e8ff,stroke:#8b5cf6
```

### UI Module Declaration Pattern

```mermaid
graph LR
    subgraph UiModule["groupsUiModule (Koin)"]
        M["single: GroupUiMapper<br/><i>→ GroupUiMapperImpl(localeProvider, resourceProvider)</i>"]
        V["viewModel: GroupsViewModel<br/><i>→ GroupsViewModel(useCase, mapper)</i>"]
        N["factory: GroupsNavigationProviderImpl<br/><i>bind NavigationProvider::class</i>"]
        S["single: GroupsScreenUiProviderImpl<br/><i>bind ScreenUiProvider::class</i>"]
    end

    M --> |"injected into"| V
    N --> |"discovered by"| MAIN["MainScreen<br/><i>koin.getAll&lt;NavigationProvider&gt;()</i>"]
    S --> |"discovered by"| MAIN

    style UiModule fill:#dbeafe
    style MAIN fill:#4a9eff,color:#fff
```

---

## 5. MVI State Management

The triad pattern used by every screen: `UiState`, `UiEvent`, `UiAction`.

```mermaid
graph LR
    subgraph UI["UI Layer"]
        SCREEN["Screen<br/><i>Stateless Composable</i>"]
        FEATURE["Feature<br/><i>Orchestrator</i>"]
    end

    subgraph VM["ViewModel"]
        ONEVENT["onEvent()"]
        STATEFLOW["StateFlow&lt;UiState&gt;"]
        ACTIONFLOW["Channel / SharedFlow&lt;UiAction&gt;"]
    end

    SCREEN -- "UiEvent<br/>(user interaction)" --> ONEVENT
    ONEVENT -- "updates" --> STATEFLOW
    ONEVENT -- "emits" --> ACTIONFLOW
    STATEFLOW -- "collectAsStateWithLifecycle()" --> FEATURE
    FEATURE -- "uiState + onEvent lambda" --> SCREEN
    ACTIONFLOW -- "collectLatest in<br/>LaunchedEffect" --> FEATURE

    FEATURE -- "snackbar / navigate" --> SIDE["Side Effects<br/><i>Snackbar, Navigation</i>"]

    style UI fill:#eff6ff
    style VM fill:#f0fdf4
    style SIDE fill:#fef3c7
```

### UiState vs UiAction — When to Use What

```mermaid
graph TB
    subgraph UiState["UiState (Persistent)"]
        S1["✅ Loading indicators"]
        S2["✅ List data (ImmutableList)"]
        S3["✅ Input field values"]
        S4["✅ Validation errors (visual)"]
        S5["✅ Scroll position"]
    end

    subgraph UiAction["UiAction (Ephemeral)"]
        A1["✅ Show Snackbar / Toast"]
        A2["✅ Navigate to another screen"]
        A3["✅ Vibration / Sound feedback"]
    end

    subgraph Never["❌ Never in UiState"]
        N1["showToast: Boolean"]
        N2["navigateToDetail: Boolean"]
        N3["one-shot success flags"]
    end

    style UiState fill:#dcfce7,stroke:#22c55e
    style UiAction fill:#dbeafe,stroke:#3b82f6
    style Never fill:#fee2e2,stroke:#ef4444
```

---

## 6. Feature vs Screen Pattern

The separation of orchestration (Feature) from rendering (Screen).

```mermaid
sequenceDiagram
    participant Nav as NavHost
    participant Feat as GroupsFeature
    participant VM as GroupsViewModel
    participant Scr as GroupsScreen

    Nav->>Feat: composable(Routes.GROUPS)
    activate Feat

    Note over Feat: koinViewModel()
    Feat->>VM: Collect uiState (StateFlow)
    Feat->>VM: Collect actions (SharedFlow)

    Note over Feat: Consumes CompositionLocals:<br/>LocalTabNavController<br/>LocalSnackbarController

    Feat->>Scr: GroupsScreen(uiState, onEvent)
    activate Scr

    Note over Scr: Pure UI rendering<br/>No ViewModel<br/>No NavController<br/>No Context

    Scr-->>Feat: onEvent(GroupsUiEvent.SelectGroup("id"))
    Feat->>VM: onEvent(event)

    VM-->>Feat: actions.emit(ShowSuccess(UiText.StringResource(...)))
    Feat->>Feat: snackbarController.showSnackbar(message.asString(context))

    deactivate Scr
    deactivate Feat
```

### Why This Pattern Enables Previews

```mermaid
graph TB
    subgraph CanPreview["✅ Previewable"]
        SCREEN["GroupsScreen(<br/>  uiState = GroupsUiState(...),<br/>  onEvent = {}<br/>)"]
    end

    subgraph CannotPreview["❌ Not Previewable"]
        BAD["GroupsScreen(<br/>  viewModel = ...,<br/>  navController = ...<br/>)"]
    end

    subgraph PreviewCode["Preview Implementation"]
        PREV["@PreviewComplete<br/>@Composable<br/>fun GroupsScreenPreview() {<br/>  PreviewThemeWrapper {<br/>    GroupsScreen(uiState = sampleState)<br/>  }<br/>}"]
    end

    SCREEN --> PREV

    style CanPreview fill:#dcfce7,stroke:#22c55e
    style CannotPreview fill:#fee2e2,stroke:#ef4444
    style PreviewCode fill:#f3e8ff,stroke:#8b5cf6
```

---

## 7. Navigation Architecture

The dual nav-controller system with CompositionLocals.

```mermaid
graph TB
    subgraph Activity["MainActivity"]
        subgraph AppNavHost["AppNavHost"]
            ROOT_NAV["LocalRootNavController<br/><i>Provided here</i>"]

            LOGIN["LoginFeature"]
            ONBOARD["OnboardingFeature"]

            subgraph MainScreen["MainScreen (Routes.MAIN)"]
                TAB_NAV["LocalTabNavController<br/><i>Provided here</i>"]
                SNACKBAR["LocalSnackbarController<br/><i>Provided here</i>"]
                TOPBAR["TopAppBar<br/><i>from ScreenUiProvider</i>"]
                FAB_AREA["FAB<br/><i>from ScreenUiProvider</i>"]
                BOTTOM["BottomNavigationBar<br/><i>from NavigationProviders</i>"]

                subgraph Tabs["Tab Content"]
                    GROUPS["GroupsFeature"]
                    EXPENSES["ExpensesFeature"]
                    BALANCES["BalancesFeature"]
                    PROFILE["ProfileFeature"]
                end

                subgraph Contributed["Via TabGraphContributor"]
                    ADD_CONTRIB["AddContributionFeature<br/><i>:features:contributions</i>"]
                    ADD_WITHDRAW["AddCashWithdrawalFeature<br/><i>:features:withdrawals</i>"]
                    MANAGE_SUB["SubunitManagementFeature<br/><i>:features:subunits</i>"]
                end
            end

            SETTINGS["SettingsFeature<br/><i>Full-screen, covers BottomBar</i>"]
            CREATE["CreateGroupFeature<br/><i>Full-screen</i>"]
            ADD_EXP["AddExpenseFeature<br/><i>Full-screen</i>"]
        end
    end

    ROOT_NAV -.-> LOGIN
    ROOT_NAV -.-> ONBOARD
    ROOT_NAV -.-> MainScreen
    ROOT_NAV -.-> SETTINGS
    ROOT_NAV -.-> CREATE
    ROOT_NAV -.-> ADD_EXP

    TAB_NAV -.-> GROUPS
    TAB_NAV -.-> EXPENSES
    TAB_NAV -.-> BALANCES
    TAB_NAV -.-> PROFILE

    BALANCES -.-> ADD_CONTRIB
    BALANCES -.-> ADD_WITHDRAW
    GROUPS -.-> MANAGE_SUB

    style Activity fill:#f8fafc,stroke:#64748b
    style AppNavHost fill:#eff6ff,stroke:#3b82f6
    style MainScreen fill:#f0fdf4,stroke:#22c55e
    style Contributed fill:#fef3c7,stroke:#f59e0b
    style Tabs fill:#fef3c7,stroke:#f59e0b
```

### Navigation Discovery (Plugin Pattern)

```mermaid
sequenceDiagram
    participant Koin as Koin DI
    participant App as AppNavHost
    participant Main as MainScreen
    participant GP as GroupsNavigationProviderImpl
    participant EP as ExpensesNavigationProviderImpl

    Note over Koin: Each feature registers:<br/>factory { XxxNavigationProviderImpl() }<br/>  bind NavigationProvider::class

    App->>Koin: koin.getAll<NavigationProvider>()
    Koin-->>App: [GroupsProvider, ExpensesProvider,<br/>BalancesProvider, ProfileProvider]

    App->>App: filterVisibleProviders(providers, selectedGroupId)
    Note over App: Features with requiresSelectedGroup=true<br/>are hidden when no group is selected

    App->>Main: MainScreen(navigationProviders, screenUiProviders, visibleProviders)

    Main->>GP: provider.buildGraph(navGraphBuilder)
    Main->>EP: provider.buildGraph(navGraphBuilder)

    Note over Main: BottomNavigationBar renders tabs<br/>from visibleProviders sorted by order
```

---

## 8. Offline-First Data Flow — Reads

UI always reads from Room. Cloud syncs into Room in the background.

```mermaid
graph LR
    subgraph UI["UI Layer"]
        COMPOSE["Compose Screen"]
    end

    subgraph Presentation["Presentation"]
        VM["ViewModel"]
        UC["UseCase"]
    end

    subgraph DataLayer["Data Layer"]
        REPO["Repository"]
    end

    subgraph Local["Local Storage"]
        ROOM["Room DB<br/><i>Single Source of Truth</i>"]
    end

    subgraph Cloud["Cloud"]
        FIRE["Firestore<br/><i>Replication target</i>"]
    end

    COMPOSE -- "collectAsState<br/>WithLifecycle()" --> VM
    VM -- "stateIn(<br/>WhileSubscribed)" --> UC
    UC -- "invoke()" --> REPO
    REPO -- "Flow&lt;List&lt;T&gt;&gt;" --> ROOM

    FIRE -- "snapshotListener<br/>(onStart)" --> REPO
    REPO -- "@Transaction<br/>upsert + selective delete" --> ROOM
    ROOM -- "Flow re-emits<br/>automatically" --> REPO

    style UI fill:#eff6ff
    style Local fill:#dcfce7
    style Cloud fill:#fef3c7
```

```mermaid
sequenceDiagram
    participant Screen
    participant VM as ViewModel
    participant UC as UseCase
    participant Repo as Repository
    participant Room
    participant Firestore

    Screen->>VM: collectAsStateWithLifecycle()
    VM->>UC: getUserGroupsFlowUseCase()
    UC->>Repo: getAllGroupsFlow()
    Repo->>Room: getGroupsFlow()
    Room-->>Screen: Instant local data ⚡

    Note over Repo: .onStart { subscribeToCloud() }
    Repo->>Firestore: snapshotListener
    Firestore-->>Repo: Remote snapshot arrives
    Repo->>Room: @Transaction: upsert + delete stale
    Room-->>Screen: Auto re-emit with fresh data 🔄
```

---

## 9. Offline-First Data Flow — Writes

Write locally first, then sync to cloud in the background.

```mermaid
sequenceDiagram
    participant User
    participant VM as ViewModel
    participant UC as AddExpenseUseCase
    participant Repo as ExpenseRepository
    participant Room
    participant Firestore
    participant OtherDevices as Other Devices

    User->>VM: onEvent(SubmitPressed)
    VM->>UC: addExpense(expense)
    UC->>Repo: addExpense(expense)

    Note over Repo: 1. Generate UUID locally
    Note over Repo: 2. Set createdAt = now()
    Note over Repo: 3. Set createdBy = currentUserId

    Repo->>Room: saveExpense(expense)
    Note over Room: UI updates INSTANTLY ⚡

    Repo->>Repo: syncScope.launch { ... }
    Repo->>Firestore: .document(localId).set(data)
    Note over Firestore: Uses SET (upsert),<br/>NEVER .add()

    Firestore-->>OtherDevices: Snapshot listener fires
    OtherDevices->>OtherDevices: Room reconciliation
```

### Write Protocol — Critical Rules

```mermaid
graph TB
    subgraph Good["✅ Correct Write Order"]
        direction TB
        G1["1. Generate UUID locally"] --> G2["2. Set timestamps locally"]
        G2 --> G3["3. Save to Room FIRST"]
        G3 --> G4["4. syncScope.launch { cloud.set() }"]
    end

    subgraph Bad["❌ Common Mistakes"]
        direction TB
        B1["Using Firestore .add()<br/><i>Generates server-side ID</i>"]
        B2["Waiting for cloud before local"]
        B3["Relying on @ServerTimestamp"]
        B4["Not catching sync failures"]
    end

    style Good fill:#dcfce7,stroke:#22c55e
    style Bad fill:#fee2e2,stroke:#ef4444
```

---

## 10. Real-Time Multi-Device Sync

How Firestore snapshot listeners reconcile with Room for multi-user, multi-device updates.

```mermaid
sequenceDiagram
    participant DeviceA as Device A
    participant RoomA as Room (Device A)
    participant Firestore
    participant RoomB as Room (Device B)
    participant DeviceB as Device B

    Note over DeviceA: User A adds an expense
    DeviceA->>RoomA: Save locally (instant)
    DeviceA->>Firestore: syncScope → .set(expense)

    Firestore-->>RoomB: Snapshot listener fires
    Note over RoomB: @Transaction:<br/>upsert remote data +<br/>delete stale local IDs
    RoomB-->>DeviceB: Flow re-emits → UI updates
```

### Room Reconciliation Strategy

```mermaid
graph TB
    subgraph Transaction["@Transaction: replaceExpensesForGroup()"]
        direction TB
        T1["1. Get remoteIds from snapshot"] --> T2["2. Get localIds from Room"]
        T2 --> T3["3. staleIds = localIds - remoteIds"]
        T3 --> T4["4. upsertAll(remoteExpenses)"]
        T4 --> T5["5. deleteByIds(staleIds)"]
    end

    subgraph Why["Why Not deleteAll + insertAll?"]
        direction TB
        W1["❌ deleteAll() wipes unsynced<br/>local data created offline"]
        W2["✅ Selective delete preserves<br/>items not yet in cloud snapshot"]
    end

    style Transaction fill:#dcfce7,stroke:#22c55e
    style Why fill:#fef3c7,stroke:#f59e0b
```

### Cloud Subscription Job Management

```mermaid
graph TB
    subgraph SingleKey["Single-Key Pattern<br/><i>(e.g., Groups — one global list)</i>"]
        SK1["private var cloudSubscriptionJob: Job?"]
        SK2["onStart {<br/>  cloudSubscriptionJob?.cancel()<br/>  cloudSubscriptionJob = syncScope.launch { ... }<br/>}"]
    end

    subgraph MultiKey["Multi-Key Pattern<br/><i>(e.g., Expenses — per group)</i>"]
        MK1["private val cloudSubscriptionJobs =<br/>  ConcurrentHashMap&lt;String, Job&gt;()"]
        MK2["onStart {<br/>  jobs[groupId]?.cancel()<br/>  jobs[groupId] = syncScope.launch { ... }<br/>}"]
    end

    subgraph Danger["⚠️ Without Cancellation"]
        D1["Each resubscription leaks a<br/>Firestore snapshot listener"]
        D2["Multiple listeners reconcile<br/>Room independently"]
        D3["Memory leak + unexpected behavior"]
    end

    style SingleKey fill:#dcfce7,stroke:#22c55e
    style MultiKey fill:#dbeafe,stroke:#3b82f6
    style Danger fill:#fee2e2,stroke:#ef4444
```

---

## 11. Data Mapping Pipeline

Two-stage mapping: Data → Domain (in `:data`) and Domain → UiModel (in `:features`).

```mermaid
graph LR
    subgraph Cloud["Cloud / Local"]
        FB_DOC["Firestore Document<br/><i>or Room Entity</i>"]
    end

    subgraph DataLayer["Data Layer (:data)"]
        DM["DocumentMapper /<br/>EntityMapper"]
    end

    subgraph DomainLayer["Domain Layer"]
        DOM_OBJ["Domain Model<br/><i>Expense(amountCents=1050,<br/>currency='EUR')</i>"]
    end

    subgraph PresLayer["Presentation Layer (:features)"]
        UI_MAP["UiMapper<br/><i>receives LocaleProvider</i>"]
        UI_MOD["UiModel<br/><i>ExpenseUiModel(<br/>formattedAmount='10.50 €')</i>"]
    end

    subgraph Compose["Compose"]
        SCR2["Screen<br/><i>renders formatted strings</i>"]
    end

    FB_DOC --> DM --> DOM_OBJ --> UI_MAP --> UI_MOD --> SCR2

    style Cloud fill:#fef3c7
    style DataLayer fill:#fffbeb
    style DomainLayer fill:#f0fdf4
    style PresLayer fill:#eff6ff
    style Compose fill:#f3e8ff
```

### Mapper Rules

```mermaid
graph TB
    subgraph Correct["✅ Correct"]
        C1["Mapper receives LocaleProvider"]
        C2["Mapper calls formatShortDate(locale)"]
        C3["Mapper calls formatCurrencyAmount(locale)"]
        C4["ViewModel injects Mapper, not LocaleProvider"]
    end

    subgraph Wrong["❌ Wrong"]
        W1["ViewModel receives LocaleProvider"]
        W2["ViewModel formats dates directly"]
        W3["Screen does string formatting"]
        W4["Passing Context to Mapper"]
    end

    style Correct fill:#dcfce7,stroke:#22c55e
    style Wrong fill:#fee2e2,stroke:#ef4444
```

---

## 12. Compose Preview System

How previews are structured across `src/main` and `src/debug` source sets.

```mermaid
graph TB
    subgraph DebugSourceSet["src/debug (Preview Utilities)"]
        subgraph CorePreview[":core:design-system/src/debug"]
            ANN["@PreviewLocales<br/>@PreviewThemes<br/>@PreviewComplete"]
            PTW["PreviewThemeWrapper"]
            MP["MappedPreview&lt;Domain, UiModel, Mapper&gt;"]
            PLP["PreviewLocaleProvider"]
            PRP["PreviewResourceProvider"]
            PNP["PreviewNavigationProviders"]
        end

        subgraph FeaturePreview[":features:groups/src/debug"]
            HELPER["GroupUiPreviewHelper"]
            PREVIEWS["GroupCardPreviews.kt"]
        end
    end

    subgraph Flow["MappedPreview Flow"]
        direction LR
        DOMAIN["Domain Object<br/><i>(sample data)</i>"]
        MAPPER2["Real Mapper<br/><i>(with preview providers)</i>"]
        UIMOD2["UiModel"]
        RENDER["Preview Composable"]

        DOMAIN --> MAPPER2 --> UIMOD2 --> RENDER
    end

    ANN --> PREVIEWS
    PTW --> PREVIEWS
    MP --> HELPER
    PLP --> MP
    PRP --> MP
    HELPER --> PREVIEWS

    style DebugSourceSet fill:#fce7f3,stroke:#ec4899
    style CorePreview fill:#f3e8ff,stroke:#8b5cf6
    style FeaturePreview fill:#fef3c7,stroke:#f59e0b
    style Flow fill:#f0fdf4,stroke:#22c55e
```

### Preview Annotations — What They Generate

```mermaid
graph LR
    subgraph PL["@PreviewLocales"]
        PL1["EN"]
        PL2["ES"]
    end

    subgraph PT["@PreviewThemes"]
        PT1["Light"]
        PT2["Dark"]
    end

    subgraph PC["@PreviewComplete"]
        PC1["EN Light"]
        PC2["EN Dark"]
        PC3["ES Light"]
        PC4["ES Dark"]
    end

    style PL fill:#dbeafe
    style PT fill:#fef3c7
    style PC fill:#dcfce7
```

---

## 13. Coroutine & Flow Architecture

How flows and dispatchers are managed across layers.

```mermaid
graph TB
    subgraph ViewModelLayer["ViewModel Layer"]
        VMSF["StateFlow via stateIn(<br/>  WhileSubscribed(5000ms, replayExpiration=0),<br/>  initialValue<br/>)"]
        VMCH["Channel&lt;UiAction&gt;<br/><i>or MutableSharedFlow</i>"]
    end

    subgraph DomainUC["UseCase"]
        UCFLOW["Returns Flow&lt;T&gt;<br/><i>from repository</i>"]
    end

    subgraph RepoLayer["Repository"]
        ROOM_FLOW["Room emits Flow&lt;List&lt;T&gt;&gt;"]
        ON_START[".onStart { subscribe cloud }"]
        SYNC_SCOPE["syncScope = CoroutineScope(ioDispatcher)<br/><i>Injected, defaults to Dispatchers.IO</i>"]
    end

    VMSF --> UCFLOW --> ROOM_FLOW
    ROOM_FLOW --> ON_START
    ON_START --> SYNC_SCOPE

    style ViewModelLayer fill:#eff6ff
    style DomainUC fill:#f0fdf4
    style RepoLayer fill:#fffbeb
```

### Testing with Injected Dispatchers

```mermaid
sequenceDiagram
    participant Test as Test Class
    participant Repo as RepositoryImpl
    participant TD as StandardTestDispatcher

    Note over Test: val testDispatcher = StandardTestDispatcher()

    Test->>Repo: RepositoryImpl(..., ioDispatcher = testDispatcher)
    Note over Repo: syncScope = CoroutineScope(testDispatcher)

    Test->>Test: runTest(testDispatcher) { ... }
    Test->>Repo: repo.deleteGroup("123")

    Note over Repo: syncScope.launch { cloud.delete() }<br/>Runs on testDispatcher (controlled)

    Test->>Test: advanceUntilIdle()
    Note over Test: All coroutines complete deterministically

    Test->>Test: coVerify { cloud.deleteGroup("123") } ✅
```

### Flow Retention — Zero-Flicker Policy

```mermaid
sequenceDiagram
    participant User
    participant Compose as Compose UI
    participant SF as StateFlow (WhileSubscribed 5s)
    participant Room

    User->>Compose: Opens Groups tab
    Compose->>SF: Starts collecting
    SF->>Room: Subscribes to Flow
    Room-->>SF: Emits groups list
    SF-->>Compose: Shows data instantly

    User->>Compose: Switches to Expenses tab
    Note over SF: Collector stopped, but...<br/>WhileSubscribed(5000ms)<br/>keeps upstream alive 5 seconds

    User->>Compose: Switches back within 5s
    Compose->>SF: Resumes collecting
    SF-->>Compose: Instant data — no reload, no shimmer ⚡

    Note over SF: If user stays away > 5s,<br/>upstream cancels and replay cache<br/>resets to initialValue (isLoading=true).<br/>Next collection sees Loading → Shimmer → Content.
```

---

## Quick Reference — File Locations

| Concept | Key File(s) |
|---|---|
| Routes | `core/design-system/.../navigation/Routes.kt` |
| NavigationProvider | `core/design-system/.../navigation/NavigationProvider.kt` |
| TabGraphContributor | `core/design-system/.../navigation/TabGraphContributor.kt` |
| ScreenUiProvider | `core/design-system/.../presentation/screen/ScreenUiProvider.kt` |
| CompositionLocals | `core/design-system/.../navigation/LocalRootNavController.kt`, `LocalTabNavController.kt` |
| SnackbarController | `core/design-system/.../presentation/snackbar/SnackbarController.kt` |
| UiText | `core/common/.../presentation/UiText.kt` |
| AppConstants | `core/common/.../constant/AppConstants.kt` |
| UiConstants | `core/design-system/.../constant/UiConstants.kt` |
| FeatureScaffold | `core/design-system/.../presentation/component/scaffold/FeatureScaffold.kt` |
| Preview Annotations | `core/design-system/src/debug/.../preview/PreviewAnnotations.kt` |
| MappedPreview | `core/design-system/src/debug/.../preview/MappedPreview.kt` |
| DI Aggregation | `app/.../di/FeatureModuleAggregations.kt` |
| AppNavHost | `features/src/main/.../navigation/AppNavHost.kt` |
| MainScreen | `features/main-entry/src/main/.../presentation/screen/MainScreen.kt` |
| Feature example | `features/profile/src/main/.../presentation/feature/ProfileFeature.kt` |
| Screen example | `features/profile/src/main/.../presentation/screen/ProfileScreen.kt` |
| ViewModel example | `features/groups/src/main/.../presentation/viewmodel/GroupsViewModel.kt` |
| Mapper example | `features/groups/src/main/.../presentation/mapper/impl/GroupUiMapperImpl.kt` |
| Tab NavProvider example | `features/groups/.../navigation/impl/GroupsNavigationProviderImpl.kt` |
| TabGraphContributor example | `features/contributions/.../navigation/impl/ContributionsTabGraphContributorImpl.kt` |
| Non-tab DI example | `features/contributions/.../di/ContributionsUiModule.kt` |
| Repository example | `data/src/main/.../repository/impl/GroupRepositoryImpl.kt` |
| Repo test example | `data/src/test/.../repository/impl/ContributionRepositoryImplTest.kt` |
| Mapper test example | `features/groups/src/test/.../mapper/impl/GroupUiMapperImplTest.kt` |

