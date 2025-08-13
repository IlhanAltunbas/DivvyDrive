package com.ilhanaltunbas.divvydrive.data.network

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor : Interceptor {
    private val username = "NDSServis"
    private val password = "ca5094ef-eae0-4bd5-a94a-14db3b8f3950"



    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            val credentials = Credentials.basic(username, password)
            val requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", credentials)
                .build()
            return chain.proceed(requestWithAuth)
        }
        return chain.proceed(originalRequest)
    }
}