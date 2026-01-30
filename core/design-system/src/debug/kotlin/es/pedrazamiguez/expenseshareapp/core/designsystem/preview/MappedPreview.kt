package es.pedrazamiguez.expenseshareapp.core.designsystem.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider

@Composable
fun <Domain, UiModel, Mapper> MappedPreview(
    domain: Domain,
    mapper: (LocaleProvider, ResourceProvider) -> Mapper,
    transform: (Mapper, Domain) -> UiModel,
    content: @Composable (UiModel) -> Unit
) {
    val context = LocalContext.current

    val localeProvider = remember(context) { PreviewLocaleProvider(context) }
    val resourceProvider = remember(context) { PreviewResourceProvider(context) }

    val mapper =
        remember(localeProvider, resourceProvider) { mapper(localeProvider, resourceProvider) }

    val uiModel = remember(domain, mapper) { transform(mapper, domain) }

    PreviewThemeWrapper {
        content(uiModel)
    }
}
