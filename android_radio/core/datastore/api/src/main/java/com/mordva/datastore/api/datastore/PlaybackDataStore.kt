package com.mordva.datastore.api.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.mordva.datastore.api.model.PlaybackData
import com.mordva.datastore.api.utils.KotlinxJsonSerializer

val Context.playbackDataStore: DataStore<PlaybackData> by dataStore(
    fileName = "playback.json",
    serializer = KotlinxJsonSerializer(
        value = PlaybackData(),
        serializer = PlaybackData.serializer(),
    ),
)
