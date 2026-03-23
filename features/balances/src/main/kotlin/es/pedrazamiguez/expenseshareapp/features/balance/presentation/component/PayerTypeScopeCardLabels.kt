package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component

/**
 * Bundles the localised labels for [PayerTypeScopeCard], keeping the composable
 * parameter count within the detekt `LongParameterList` threshold.
 *
 * @param title                Localised card title (e.g. "Contributing for").
 * @param groupLabel           Localised label for the GROUP option.
 * @param personalLabel        Localised label for the USER (personal) option.
 * @param subunitLabelTemplate Format string for a sub-unit option (e.g. "For %1\$s").
 *                             The component substitutes the sub-unit name via [String.format].
 */
data class PayerTypeScopeCardLabels(
    val title: String,
    val groupLabel: String,
    val personalLabel: String,
    val subunitLabelTemplate: String
)
