package com.mordva.feature.home.state

internal sealed interface HomeScreenRadioState {
    data object Error : HomeScreenRadioState

    data object Loading : HomeScreenRadioState

    data class Success(
        val id: Int,
        val title: String,
        val description: String = "",
        val artworkUrl: String? = null,
        val streamUrl: String = "",
        val elapsedMs: Long = 0L,
        val durationMs: Long? = null,
    ) : HomeScreenRadioState {
        val progress: Float?
            get() = durationMs
                ?.takeIf { it > 0L }
                ?.let { duration ->
                    (elapsedMs.toDouble() / duration)
                        .coerceIn(0.0, 1.0)
                        .toFloat()
                }
    }
}

internal fun HomeScreenRadioState.getStationId(): Int? {
    return (this as? HomeScreenRadioState.Success)?.id
}
