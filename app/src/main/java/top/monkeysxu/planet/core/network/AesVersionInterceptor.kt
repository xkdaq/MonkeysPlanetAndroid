package top.monkeysxu.planet.core.network

import okhttp3.Interceptor
import okhttp3.Response

class AesVersionInterceptor(
    private val version: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("x-version", version)
            .header("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
