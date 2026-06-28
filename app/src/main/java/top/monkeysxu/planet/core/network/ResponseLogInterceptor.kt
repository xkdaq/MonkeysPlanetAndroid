package top.monkeysxu.planet.core.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * 响应体日志拦截器（调试用）
 * 打印原始响应 JSON，帮助排查数据解析问题
 */
class ResponseLogInterceptor(private val tag: String = "API_RAW") : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val body = response.body
        val contentType = body?.contentType()
        val bodyString = body?.string() ?: ""

        // 打印到 Logcat，每段最多 3000 字符（Logcat 单条有限制）
        val url = request.url.toString()
        val status = response.code
        Log.d(tag, "◆ [$status] $url")
        if (bodyString.isNotEmpty()) {
            bodyString.chunked(3000).forEachIndexed { i, chunk ->
                Log.d(tag, "  body[$i]: $chunk")
            }
        }

        // 必须重新构建 body（stream 只能读一次）
        val newBody = bodyString.toResponseBody(contentType)
        return response.newBuilder().body(newBody).build()
    }
}
