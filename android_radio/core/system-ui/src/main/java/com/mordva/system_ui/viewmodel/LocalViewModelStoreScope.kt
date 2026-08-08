package com.mordva.system_ui.viewmodel

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

/**
 * Реестр [ViewModelStore]-ов, хранимый на уровне Activity.
 * Переживает смену конфигурации (поворот), поскольку сам является [ViewModel].
 */
internal class ViewModelStoreRegistry : ViewModel() {
    private val stores = mutableMapOf<String, ViewModelStore>()

    fun getOrCreate(key: String): ViewModelStore = stores.getOrPut(key) { ViewModelStore() }

    fun remove(key: String) {
        stores.remove(key)?.clear()
    }

    override fun onCleared() {
        stores.values.forEach { it.clear() }
        stores.clear()
    }
}

/**
 * Предоставляет изолированный [ViewModelStoreOwner], привязанный к lifecycle этого composable.
 *
 * Все ViewModel, созданные внутри [content] через `koinViewModel` / `viewModel`,
 * будут уничтожены (вызов `onCleared()`) как только composable покинет composition
 * **по причине, отличной от смены конфигурации** (поворот экрана).
 *
 * При повороте экрана store сохраняется в Activity-level [ViewModelStoreRegistry]
 * и повторно используется после пересоздания composition.
 *
 * Идеально подходит для BottomSheet / Dialog, чтобы их ViewModel не
 * оставались живыми на уровне Activity после закрытия.
 *
 * Использование:
 * ```kotlin
 * WithLocalViewModelStoreOwner {
 *     LocationBottomSheet(...)
 * }
 * ```
 */
@Composable
fun WithLocalViewModelStoreOwner(content: @Composable () -> Unit) {
    val activity = LocalContext.current as Activity

    // Activity-scoped реестр — переживает rotation
    val registry = viewModel<ViewModelStoreRegistry>()

    // Стабильный ключ, который восстанавливается после rotation через rememberSaveable
    val key = rememberSaveable { UUID.randomUUID().toString() }

    val store = remember(key) { registry.getOrCreate(key) }

    DisposableEffect(key) {
        onDispose {
            // Очищаем store только если это не смена конфигурации.
            // При повороте activity.isChangingConfigurations == true,
            // поэтому store остаётся жить в registry и переиспользуется.
            if (!activity.isChangingConfigurations) {
                registry.remove(key)
            }
        }
    }

    val owner = remember(store) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = store
        }
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        content()
    }
}
