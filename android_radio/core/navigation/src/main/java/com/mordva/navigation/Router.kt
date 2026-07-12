package com.mordva.navigation

import androidx.navigation3.runtime.NavKey
import com.mordva.navigation.route.HomeRoute

object Router {

    val startDestination: NavKey = HomeRoute

    val allRoutes: Set<NavKey> = setOf(
        HomeRoute,
    )
}
