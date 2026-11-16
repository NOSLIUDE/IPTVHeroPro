package com.iptvhero.pro.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder

class ProxyInterceptor : Interceptor {
    @Volatile
    var proxyPrefix: String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentProxy = proxyPrefix.trim()

        if (currentProxy.isEmpty()) {
            return chain.proceed(originalRequest)
        }

        val originalUrlString = originalRequest.url.toString()
        if (originalUrlString.startsWith(currentProxy)) {
            return chain.proceed(originalRequest)
        }

        val encodedTarget = URLEncoder.encode(originalUrlString, "UTF-8")
        val proxiedUrlString = if (currentProxy.endsWith("=") || currentProxy.endsWith("/")) {
            "$currentProxy$encodedTarget"
        } else {
            "$currentProxy?url=$encodedTarget"
        }

        val newHttpUrl = proxiedUrlString.toHttpUrlOrNull()
        return if (newHttpUrl != null) {
            val newRequest = originalRequest.newBuilder()
                .url(newHttpUrl)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
