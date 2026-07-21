package es.pedrazamiguez.splittrip.features.settlement.presentation.feature

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.navigation.SharedElementKeys
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.FeatureScaffold
import es.pedrazamiguez.splittrip.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.splittrip.features.settlement.R

@Composable
fun MyPositionFeature(
    modifier: Modifier = Modifier
) {
    SharedTransitionSurface(
        sharedElementKey = SharedElementKeys.MY_POSITION,
        modifier = modifier
    ) {
        FeatureScaffold(
            currentRoute = Routes.MY_POSITION
        ) {
            Text(text = stringResource(R.string.my_position_title))
        }
    }
}
