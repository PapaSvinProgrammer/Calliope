package com.mordva.datastore.api.manager

interface CryptoManager {
    fun encrypt(plainText: String): String
    fun decrypt(encryptedData: String): String
}