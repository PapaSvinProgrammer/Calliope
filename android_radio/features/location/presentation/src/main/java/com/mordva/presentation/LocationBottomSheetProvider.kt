package com.mordva.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import com.mordva.system_ui.viewmodel.WithLocalViewModelStoreOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheetProvider(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    ),
) {
    WithLocalViewModelStoreOwner {
        LocationBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
        )
    }
}