package es.pedrazamiguez.splittrip.provider.impl

import es.pedrazamiguez.splittrip.core.common.provider.SupportEmailAddressProvider
import es.pedrazamiguez.splittrip.domain.service.AppConfigService

class SupportEmailAddressProviderImpl(
    private val appConfigService: AppConfigService
) : SupportEmailAddressProvider {
    override fun getSupportEmailAddress(): String {
        return appConfigService.supportEmailAddress.value
    }
}
