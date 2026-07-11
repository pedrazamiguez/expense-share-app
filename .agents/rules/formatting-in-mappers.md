## Formatting in Mappers

Formatting belongs in Mappers, not ViewModels and not Domain Services. Mappers receive `LocaleProvider`. Domain Services must NEVER contain `formatShareForInput()`, `formatAmountForDisplay()`, or any human-readable formatting method.
