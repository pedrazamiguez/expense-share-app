## Feature vs Screen Pattern

`*Feature` (orchestrator composable) holds ViewModel, collects flows, consumes `LocalTopPillController`/`LocalTabNavController`. `*Screen` is stateless — takes `UiState` + event lambdas only.
