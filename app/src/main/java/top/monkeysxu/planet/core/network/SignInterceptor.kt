package top.monkeysxu.planet.core.network

import okhttp3.Interceptor
import top.monkeysxu.planet.BuildConfig
import okhttp3.Response
import java.math.BigInteger
import java.security.MessageDigest

class SignInterceptor : Interceptor {

    companion object {
        // 从 BuildConfig 读取（值来自 secrets.properties）
        private val SIGN_KEY: String = BuildConfig.SIGN_KEY
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val timestamp = System.currentTimeMillis().toString()
        val path = request.url.encodedPath
        val sign = md5(SIGN_KEY + timestamp + path)
        val newRequest = request.newBuilder()
            .header("X-Timestamp", timestamp)
            .header("X-Sign", sign)
            .build()
        return chain.proceed(newRequest)
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray()))
            .toString(16)
            .padStart(32, '0')
    }
}
