package com.mordva.presentation.di

import com.mordva.domain.location.di.locationDomainModule
import com.mordva.presentation.LocationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val locationPresentationModule = module {
    viewModelOf(::LocationViewModel)
}

val locationModules = listOf(
    locationPresentationModule,
    locationDomainModule,
)