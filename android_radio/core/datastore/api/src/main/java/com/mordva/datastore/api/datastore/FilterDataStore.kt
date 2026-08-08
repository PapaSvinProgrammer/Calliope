package com.mordva.datastore.api.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.mordva.datastore.api.model.FilterData
import com.mordva.datastore.api.utils.KotlinxJsonSerializer

val Context.filterDataStore: DataStore<FilterData> by dataStore(
    fileName = "filters.json",
    serializer = KotlinxJsonSerializer(
        value = FilterData(),
        serializer = FilterData.serializer(),
    ),
)
