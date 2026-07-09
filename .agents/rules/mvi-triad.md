## MVI Triad

Every screen gets `UiState` (ImmutableList), `UiEvent` (sealed interface), and `UiAction` (side-effects via Channel/SharedFlow). Never put one-shot events in `UiState`.
