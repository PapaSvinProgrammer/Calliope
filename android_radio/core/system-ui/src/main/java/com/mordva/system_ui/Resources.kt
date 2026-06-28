package com.mordva.system_ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp

object Resources {
    object Dimens {
        val DP1: Dp
            @Composable
            get() = dimensionResource(R.dimen.DP1)

        val DP5: Dp
            @Composable
            get() = dimensionResource(R.dimen.DP5)

        val DP8: Dp
            @Composable
            get() = dimensionResource(R.dimen.DP8)

        val DP10: Dp
            @Composable
            get() = dimensionResource(R.dimen.DP10)

        val DP12: Dp
            @Composable
            get() = dimensionResource(R.dimen.DP12)

        val DP16: Dp
            @Composable
            get() = dimensionResource(R.dimen.DP16)

        val DP24: Dp
            @Composable
            get() = dimensionResource(R.dimen.DP24)

        val DP30: Dp
            @Composable
            get() = dimensionResource(R.dimen.DP30)
    }
}
