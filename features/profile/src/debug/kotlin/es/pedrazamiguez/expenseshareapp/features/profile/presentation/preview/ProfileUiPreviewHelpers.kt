package es.pedrazamiguez.expenseshareapp.features.profile.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.MappedPreview
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.mapper.impl.ProfileUiMapperImpl
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.model.ProfileUiModel

@Composable
fun ProfileUiPreviewHelper(
    domainUser: User = PREVIEW_USER,
    content: @Composable (ProfileUiModel) -> Unit
) {
    MappedPreview(
        domain = domainUser,
        mapper = { localeProvider, _ ->
            ProfileUiMapperImpl(localeProvider)
        },
        transform = { mapper, domain ->
            mapper.toProfileUiModel(domain)
        },
        content = content
    )
}
