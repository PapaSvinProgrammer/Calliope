package com.mordva.sdk.api

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

data class AuthSdkConfig(
    val context: Context,
    val apiUrl: String = "",
    val httpClient: OkHttpClient = OkHttpClient(),
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    },
)