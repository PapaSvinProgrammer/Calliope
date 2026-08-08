package com.mordva.datastore.impl

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.mordva.datastore.api.CryptoManager

internal class TinkCryptoManager(context: Context) : CryptoManager {
    init {
        AeadConfig.register()
    }

    private val keyTemplate = KeyTemplate.createFrom(PredefinedAeadParameters.AES128_GCM)

    private val keysetHandle: KeysetHandle = AndroidKeysetManager.Builder()
        .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
        .withKeyTemplate(keyTemplate)
        .withMasterKeyUri(MASTER_KEY)
        .build()
        .keysetHandle

    private val aead: Aead = keysetHandle.getPrimitive(
        RegistryConfiguration.get(),
        Aead::class.java,
    )

    override fun encrypt(plainText: String): String {
        val encryptedBytes = aead.encrypt(plainText.toByteArray(), null)
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    override fun decrypt(encryptedData: String): String {
        val encryptedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)
        val decryptedBytes = aead.decrypt(encryptedBytes, null)
        return String(decryptedBytes)
    }

    private companion object {
        const val KEYSET_NAME = "keyset_pref"
        const val PREF_FILE_NAME = "keyset_file"
        const val MASTER_KEY = "android-keystore://tink_master_key"
    }
}