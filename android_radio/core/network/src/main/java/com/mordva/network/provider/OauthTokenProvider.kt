package com.mordva.network.provider

import java.util.concurrent.atomic.AtomicReference

internal class OauthTokenProvider {
    private val token = AtomicReference<String>()

    fun provide(): String = token.get()

    fun update() {
        token.set("")
    }
}