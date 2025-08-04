package com.texthip.thip.utils.auth

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        request
            .addHeader(
                "Authorization",
                "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc1NDI4MjMzNiwiZXhwIjoxNzU2ODc0MzM2fQ.NG_xDSdh8A6egIX2EAFtsqDO4lmFphTzqgzHC-r8eXY"
            )
        return chain.proceed(request.build())
    }
}