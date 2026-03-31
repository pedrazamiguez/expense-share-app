package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input

/**
 * Bundles the localised labels for [PayerTypeScopeCard], keeping the composable
 * parameter count within the detekt `LongParameterList` threshold.
 *
 * @param title                Localised card title (e.g. "Contributing for").
 * @param groupLabel           Localised label for the GROUP option.
 * @param personalLabel        Localised label for the USER (personal) option.
 * @param subunitLabelTemplate Format string for a subunit option (e.g. "For %1$s").
 *                             The component substitutes the subunit name via [String.format].
 */
data class PayerTypeScopeCardLabels(
    val title: String,
    val groupLabel: String,
    val personalLabel: String,
    val subunitLabelTemplate: String
)
