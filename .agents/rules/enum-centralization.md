## Enum Centralization

Domain enums (e.g., `AppLanguage`, `Currency`) must be the single source of truth for parsing codes, fallback defaults, and validation. Never duplicate string-matching logic or locale fallback checks in ViewModels or presentation layers.
