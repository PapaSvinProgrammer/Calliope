package com.mordva.datastore.api.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.mordva.datastore.api.model.CityData
import com.mordva.datastore.api.utils.KotlinxJsonSerializer

val Context.cityDataStore: DataStore<CityData> by dataStore(
    fileName = CityDataStoreConfig.FILE_NAME,
    serializer = KotlinxJsonSerializer(
        value = CityData(),
        serializer = CityData.serializer(),
    ),
)

private object CityDataStoreConfig {
    const val FILE_NAME = "city.json"
}
