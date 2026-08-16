package es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions

import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Sitemap
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.User
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.UsersGroup
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayerTypeIconExtensionsTest {

    @Test
    fun `toIconVector returns User icon for USER scope`() {
        assertEquals(TablerIcons.Outline.User, PayerType.USER.toIconVector())
    }

    @Test
    fun `toIconVector returns UsersGroup icon for GROUP scope`() {
        assertEquals(TablerIcons.Outline.UsersGroup, PayerType.GROUP.toIconVector())
    }

    @Test
    fun `toIconVector returns Sitemap icon for SUBUNIT scope`() {
        assertEquals(TablerIcons.Outline.Sitemap, PayerType.SUBUNIT.toIconVector())
    }
}
