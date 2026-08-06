package com.mordva.presentation.di

import com.mordva.domain.location.di.filterDomainModule
import com.mordva.presentation.FilterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val filterPresentationModule = module {
    viewModelOf(::FilterViewModel)
}

val filterModules = listOf(
    filterPresentationModule,
    filterDomainModule,
)