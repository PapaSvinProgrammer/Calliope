package com.mordva.datastore.api.provider

import android.content.Context
import com.mordva.datastore.api.CryptoManager
import com.mordva.datastore.impl.TinkCryptoManager

object CryptoManagerProvider {
    fun provider(context: Context): CryptoManager {
        return TinkCryptoManager(context)
    }
}