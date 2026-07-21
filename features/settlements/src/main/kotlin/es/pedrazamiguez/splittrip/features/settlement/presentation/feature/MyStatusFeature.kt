package es.pedrazamiguez.splittrip.features.settlement.presentation.feature

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.FeatureScaffold
import es.pedrazamiguez.splittrip.features.settlement.R

@Composable
fun MyStatusFeature(
    modifier: Modifier = Modifier
) {
    FeatureScaffold(
        currentRoute = Routes.MY_STATUS,
        modifier = modifier
    ) {
        Text(text = stringResource(R.string.my_status_title))
    }
}
