package com.mordva.player.impl.utils

import androidx.media3.common.Player

internal object ExoPlayerLogger {

    fun mapToDebugString(player: Player, events: Player.Events): String {
        val errorInfo = if (player.playerError != null) {
            """
            |   💥 ERROR: ${player.playerError!!.errorCodeName}
            |      Code: ${player.playerError!!.errorCode}
            |      Message: ${player.playerError!!.message}
            """.trimMargin()
        } else {
            ""
        }

        return buildString {
            appendLine("🎬 [EVENTS] $events")
            appendLine("----------------------------------------")
            appendLine("📊 STATE:")
            appendLine("   Code: ${player.playbackState}")
            appendLine("   Is Playing: ${player.isPlaying}")
            appendLine("   Play When Ready: ${player.playWhenReady}")
            appendLine("   Is Loading: ${player.isLoading}")
            appendLine("   Suppression Reason: ${player.playbackSuppressionReason}")
            appendLine("----------------------------------------")
            appendLine("🎬 MEDIA ITEM:")
            appendLine("   Index: ${player.currentMediaItemIndex}")
            appendLine("   Media ID: ${player.currentMediaItem?.mediaId ?: "null"}")
            appendLine("----------------------------------------")
            appendLine("⏱️ POSITION & BUFFER:")
            appendLine("   Current: ${player.currentPosition} ms")
            appendLine("   Duration: ${player.duration} ms")
            appendLine("   Buffered: ${player.bufferedPosition} ms")
            appendLine("   Percentage: ${player.bufferedPercentage}%")
            appendLine("----------------------------------------")
            append(errorInfo)
            appendLine("========================================")
        }
    }

    fun mapToProductionString(player: Player, events: Player.Events): String {
        val base = "State: ${player.playbackState}, Events: $events"
        return if (player.playerError != null) {
            "$base | ERROR: ${player.playerError!!.errorCodeName}"
        } else {
            base
        }
    }
}
