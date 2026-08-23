package es.pedrazamiguez.splittrip.features.settings.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BrandGithub
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BrandLinkedin
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.World
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SectionCard
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.model.DeveloperInfoUiState

@Composable
fun DeveloperLinksCard(
    uiState: DeveloperInfoUiState,
    onLinkClick: (String) -> Unit
) {
    SectionCard(title = stringResource(R.string.developer_info_section_links)) {
        if (uiState.githubUrl.isNotBlank()) {
            DeveloperLinkRow(
                icon = TablerIcons.Outline.BrandGithub,
                title = stringResource(R.string.developer_info_link_github),
                url = uiState.githubUrl,
                onClick = onLinkClick
            )
        }
        if (uiState.splitTripRepoUrl.isNotBlank()) {
            DeveloperLinkRow(
                icon = TablerIcons.Outline.BrandGithub,
                title = stringResource(R.string.developer_info_link_splittrip),
                url = uiState.splitTripRepoUrl,
                onClick = onLinkClick
            )
        }
        if (uiState.linkedinUrl.isNotBlank()) {
            DeveloperLinkRow(
                icon = TablerIcons.Outline.BrandLinkedin,
                title = stringResource(R.string.developer_info_link_linkedin),
                url = uiState.linkedinUrl,
                onClick = onLinkClick
            )
        }
        if (uiState.portfolioUrl.isNotBlank()) {
            DeveloperLinkRow(
                icon = TablerIcons.Outline.World,
                title = stringResource(R.string.developer_info_link_portfolio),
                url = uiState.portfolioUrl,
                onClick = onLinkClick
            )
        }
    }
}
