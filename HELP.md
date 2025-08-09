# Module Dependencies (Hexagonal Architecture Style)

## Dependency Table

| Module           | Depends on                 | Notes                                                                                |
|------------------|----------------------------|--------------------------------------------------------------------------------------|
| **:app**         | `:core`, all `:ui:*`       | Entry point, sets up navigation, DI, and ties modules together.                      |
| **:core**        | *(nothing)*                | Android-specific utilities, themes, navigation helpers. No domain logic.             |
| **:domain**      | *(nothing)*                | Pure Kotlin business logic and domain models. Defines repository interfaces (ports). |
| **:data**        | `:domain`, `:core` *(opt)* | Implements domain ports. May use Android SDK if needed.                              |
| **:ui:auth**     | `:domain`, `:core`         | UI for authentication, uses domain ports and Android utilities.                      |
| **:ui:group**    | `:domain`, `:core`         | UI for group management.                                                             |
| **:ui:expense**  | `:domain`, `:core`         | UI for expense tracking.                                                             |
| **:ui:balance**  | `:domain`, `:core`         | UI for balance display.                                                              |
| **:ui:settings** | `:domain`, `:core`         | UI for settings.                                                                     |
| **:common**      | `:core`                    | Shared UI components and Android-specific services.                                  |

---

## Dependency Graph

```mermaid
graph TD

    subgraph App
        APP[:app]
    end

    subgraph Core
        CORE[:core]
    end

    subgraph Domain
        DOMAIN[:domain]
    end

    subgraph Data
        DATA[:data]
    end

    subgraph UI
        UI_AUTH[:ui:auth]
        UI_GROUP[:ui:group]
        UI_EXPENSE[:ui:expense]
        UI_BALANCE[:ui:balance]
        UI_SETTINGS[:ui:settings]
    end

    subgraph Common
        COMMON[:common]
    end

    APP --> CORE
    APP --> UI_AUTH
    APP --> UI_GROUP
    APP --> UI_EXPENSE
    APP --> UI_BALANCE
    APP --> UI_SETTINGS

    UI_AUTH --> DOMAIN
    UI_GROUP --> DOMAIN
    UI_EXPENSE --> DOMAIN
    UI_BALANCE --> DOMAIN
    UI_SETTINGS --> DOMAIN

    UI_AUTH --> CORE
    UI_GROUP --> CORE
    UI_EXPENSE --> CORE
    UI_BALANCE --> CORE
    UI_SETTINGS --> CORE

    COMMON --> CORE

    DATA --> DOMAIN
    DATA --> CORE

```

---

### Key Rules
- **Domain is independent**: no dependencies on Android, Core, Data, or UI.
- **Data depends on Domain**: implements ports defined in Domain.
- **UI depends on Domain**: calls ports, receives domain models.
- **Core** is Android-specific: used by UI and Data, but never by Domain.
