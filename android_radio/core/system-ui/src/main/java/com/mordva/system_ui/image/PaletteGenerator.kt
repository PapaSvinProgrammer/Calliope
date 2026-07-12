package com.mordva.system_ui.image

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaletteGenerator {
    private val _paletteState = MutableStateFlow<Palette?>(null)
    val paletteState = _paletteState.asStateFlow()

    fun createPaletteAsync(bitmap: Bitmap?) {
        if (bitmap == null) return

        Palette.from(bitmap).generate { palette ->
            _paletteState.value = palette
        }
    }
}