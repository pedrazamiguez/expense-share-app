package es.pedrazamiguez.expenseshareapp.features.profile.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetCurrentUserProfileUseCase
import es.pedrazamiguez.expenseshareapp.features.profile.navigation.impl.ProfileNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.mapper.ProfileUiMapper
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.mapper.impl.ProfileUiMapperImpl
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.screen.impl.ProfileScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val profileUiModule = module {

    single<ProfileUiMapper> {
        ProfileUiMapperImpl(
            localeProvider = get<LocaleProvider>()
        )
    }

    viewModel {
        ProfileViewModel(
            getCurrentUserProfileUseCase = get<GetCurrentUserProfileUseCase>(),
            profileUiMapper = get<ProfileUiMapper>()
        )
    }

    factory { ProfileNavigationProviderImpl() } bind NavigationProvider::class
    single { ProfileScreenUiProviderImpl() } bind ScreenUiProvider::class
}
