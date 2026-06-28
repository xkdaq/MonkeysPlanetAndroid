package top.monkeysxu.planet.core.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class AesDecryptInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) {
            val body = response.body
            val contentType = body?.contentType()
            val bodyString = body?.string()
            if (!bodyString.isNullOrEmpty()) {
                return try {
                    val decryptedJson = AesCipher.decrypt(bodyString)
                    val newBody = decryptedJson.toResponseBody(contentType)
                    response.newBuilder().body(newBody).build()
                } catch (e: Exception) {
                    // 解密失败，返回原始响应
                    val originalBody = bodyString.toResponseBody(contentType)
                    response.newBuilder().body(originalBody).build()
                }
            }
        }
        return response
    }
}
