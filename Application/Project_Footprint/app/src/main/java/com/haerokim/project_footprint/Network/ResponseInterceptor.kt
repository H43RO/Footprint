package com.haerokim.project_footprint.Network

import okhttp3.Interceptor
import okhttp3.Response

class ResponseInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val modified = response.newBuilder()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build()

        return modified
    }
}