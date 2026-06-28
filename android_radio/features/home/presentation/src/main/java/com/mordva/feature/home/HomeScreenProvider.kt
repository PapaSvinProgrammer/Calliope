package com.mordva.feature.home

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenProvider() {
    HomeScreen(viewModel = koinViewModel())
}