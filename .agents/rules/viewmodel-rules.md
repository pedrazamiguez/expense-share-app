## ViewModel Rules

ViewModels ONLY inject UseCases, Mappers, and Domain Services. NEVER inject `Context`, `LocaleProvider`, Repositories, or other ViewModels.
