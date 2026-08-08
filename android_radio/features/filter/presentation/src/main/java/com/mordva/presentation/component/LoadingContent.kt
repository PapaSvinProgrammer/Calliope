package com.mordva.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        LoadingIndicator(modifier = Modifier.align(Alignment.Center))
    }
}