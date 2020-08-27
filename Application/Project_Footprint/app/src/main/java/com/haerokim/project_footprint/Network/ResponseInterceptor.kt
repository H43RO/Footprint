package com.haerokim.project_footprint.Network

import okhttp3.Interceptor
import okhttp3.Response

/**
 *  사용자 입력을 HTTP Request에 담는 상황에 사용하는 Interceptor Class
 *  - Keyword 기반 History 조회 기능에서, 사용자의 입력을 UTF-8로 전송함
 **/

class ResponseInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val modified = response.newBuilder()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build()

        return modified
    }
}