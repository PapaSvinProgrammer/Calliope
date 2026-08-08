package com.mordva.feature.search.presentation.di

import com.mordva.domain.di.homeDomainModule
import com.mordva.feature.search.presentation.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val searchPresentationModule = module {
    viewModelOf(::SearchViewModel)
}

val searchModules = listOf(
    searchPresentationModule,
    homeDomainModule,
)
