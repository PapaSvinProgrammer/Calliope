package com.mordva.system_ui.image

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.mordva.system_ui.R
import com.mordva.system_ui.Resources
import com.mordva.system_ui.shimmer.shimmer
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

@Composable
fun ImageSquareWithShadow(
    model: String?,
    modifier: Modifier = Modifier,
    paletteGenerator: PaletteGenerator = remember { PaletteGenerator() }
) {
    val context = LocalContext.current
    val shadowColorState = paletteGenerator.paletteState.toColorState()

    val animatedShadowColor by animateColorAsState(
        targetValue = shadowColorState.value,
        animationSpec = tween(durationMillis = 1200),
    )

    val painter = rememberAsyncImagePainter(createImageRequest(context, model))
    val asyncImageState by painter.state.collectAsStateWithLifecycle()

    LaunchedEffect(asyncImageState) {
        if (asyncImageState is AsyncImagePainter.State.Success) {
            val bitmap =
                (asyncImageState as AsyncImagePainter.State.Success).result.image.toBitmap()
            paletteGenerator.createPaletteAsync(bitmap)
        }
    }

    when (asyncImageState) {
        is AsyncImagePainter.State.Empty,
        is AsyncImagePainter.State.Loading,
            -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(Resources.Dimens.DP10))
                    .shimmer()
            )
        }

        is AsyncImagePainter.State.Error -> ErrorImageContent(modifier)

        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .dropShadow(
                        shape = RoundedCornerShape(Resources.Dimens.DP10),
                        shadow = Shadow(
                            radius = Resources.Dimens.DP10,
                            spread = Resources.Dimens.DP5,
                            color = animatedShadowColor,
                            offset = DpOffset(0.dp, 0.dp),
                        )
                    )
                    .clip(RoundedCornerShape(Resources.Dimens.DP10))
            )
        }
    }
}

@Composable
private fun ErrorImageContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(Resources.Dimens.DP10),
            )
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_radio),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(40.dp)
        )
    }
}

@Composable
private fun StateFlow<Palette?>.toColorState(): State<Color> {
    val defaultColor = MaterialTheme.colorScheme.background

    val colorFlow = remember(this) {
        map { palette ->
            val res = palette?.extractShadowColor(defaultColor.toArgb()) ?: defaultColor
            res.copy()
        }
    }

    return colorFlow.collectAsStateWithLifecycle(defaultColor)
}

private fun Palette.extractShadowColor(defaultColor: Int): Color {
    val colorInt = getVibrantColor(
        getLightVibrantColor(
            getMutedColor(
                getDominantColor(defaultColor)
            )
        )
    )
    return Color(colorInt)
}

private fun createImageRequest(context: Context, model: String?): ImageRequest {
    return ImageRequest.Builder(context)
        .data(model)
        .allowHardware(false)
        .build()
}
