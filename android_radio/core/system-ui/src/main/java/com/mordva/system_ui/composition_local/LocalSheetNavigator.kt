package com.mordva.system_ui.composition_local

import androidx.compose.runtime.staticCompositionLocalOf
import com.mordva.system_ui.sheet.AppSheet

/**
 * CompositionLocal для показа BottomSheet из любого места UI.
 *
 * Использование в composable:
 * ```kotlin
 * val showSheet = LocalSheetNavigator.current
 * Button(onClick = { showSheet(AppSheet.Location) }) { ... }
 * ```
 *
 * Провайдер находится в MainActivity:
 * ```kotlin
 * CompositionLocalProvider(LocalSheetNavigator provides { sheet -> currentSheet = sheet }) {
 *     ...
 * }
 * ```
 */
val LocalSheetNavigator = staticCompositionLocalOf<(AppSheet) -> Unit> {
    error("LocalSheetNavigator not provided. Wrap your UI with CompositionLocalProvider.")
}
