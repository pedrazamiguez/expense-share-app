package es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions

import androidx.compose.ui.graphics.vector.ImageVector
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Sitemap
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.User
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.UsersGroup
import es.pedrazamiguez.splittrip.domain.enums.PayerType

fun PayerType.toIconVector(): ImageVector = when (this) {
    PayerType.USER -> TablerIcons.Outline.User
    PayerType.GROUP -> TablerIcons.Outline.UsersGroup
    PayerType.SUBUNIT -> TablerIcons.Outline.Sitemap
}
