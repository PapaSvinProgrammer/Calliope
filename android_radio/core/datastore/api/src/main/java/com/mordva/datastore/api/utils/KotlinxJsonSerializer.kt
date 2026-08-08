package com.mordva.datastore.api.utils

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal class KotlinxJsonSerializer<T>(
    value: T,
    private val serializer: KSerializer<T>,
    private val json: Json = Json,
) : Serializer<T> {
    override val defaultValue: T = value

    override suspend fun readFrom(input: InputStream): T {
        return try {
            val decodeString = input.bufferedReader().use { it.readText() }
            json.decodeFromString(serializer, decodeString)
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read", serialization)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        val encodedString = json.encodeToString(serializer, t)
        output.writer().use { it.write(encodedString) }
    }
}