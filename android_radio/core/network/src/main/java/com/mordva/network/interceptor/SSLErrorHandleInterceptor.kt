package com.mordva.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.net.ssl.SSLHandshakeException

internal class SSLErrorHandleInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: SSLHandshakeException) {
            // TODO: Добавить логирование в AppMetric
            throw e
        }
    }
}
