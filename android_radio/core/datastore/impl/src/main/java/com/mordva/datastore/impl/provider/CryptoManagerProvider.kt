package com.mordva.datastore.impl.provider

import android.content.Context
import com.mordva.datastore.api.manager.CryptoManager
import com.mordva.datastore.impl.manager.TinkCryptoManager

object CryptoManagerProvider {
    fun provider(context: Context): CryptoManager {
        return TinkCryptoManager(context)
    }
}