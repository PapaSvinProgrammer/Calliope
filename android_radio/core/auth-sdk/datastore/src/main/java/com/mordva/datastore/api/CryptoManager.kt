package com.mordva.datastore.api

interface CryptoManager {
    fun encrypt(plainText: String): String
    fun decrypt(encryptedData: String): String
}