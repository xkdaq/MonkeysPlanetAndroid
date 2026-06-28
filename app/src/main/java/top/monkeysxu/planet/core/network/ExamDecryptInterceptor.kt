package top.monkeysxu.planet.core.network

import android.util.Base64
import top.monkeysxu.planet.BuildConfig
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 考试模块加密响应解密拦截器
 * 
 * 后端对 startPractice / getQuestion 接口的 data 字段做了 AES 加密
 * 响应格式: {"code":0, "msg":"success", "data":"<加密Base64字符串>", "encrypted":true}
 * 解密后还原: {"code":0, "msg":"success", "data":{...实际数据}}
 * 
 * 参考小程序 crypto.js 的解密实现，使用 AES/CBC/PKCS5Padding
 */
class ExamDecryptInterceptor : Interceptor {

    companion object {
        private const val TAG = "ExamDecrypt"
        // 从 BuildConfig 读取（值来自 secrets.properties）
        private val AES_KEY: String = BuildConfig.AES_KEY
        private val AES_IV: String = BuildConfig.AES_IV
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) return response

        val body = response.body ?: return response
        val contentType = body.contentType()
        val bodyString = body.string()

        if (bodyString.isNullOrEmpty()) {
            val newBody = (bodyString ?: "").toResponseBody(contentType)
            return response.newBuilder().body(newBody).build()
        }

        // 快速判断：如果不包含 "encrypted" 关键字，直接跳过
        if (!bodyString.contains("\"encrypted\"")) {
            val newBody = bodyString.toResponseBody(contentType)
            return response.newBuilder().body(newBody).build()
        }

        return try {
            val json = JSONObject(bodyString)
            // 检查是否是加密响应
            if (json.optBoolean("encrypted", false)) {
                val encryptedData = json.optString("data", "")
                Log.d(TAG, "检测到加密响应, encrypted=true, data长度=${encryptedData.length}")
                
                if (encryptedData.isNotEmpty()) {
                    // 使用自带的 AES 解密（不依赖 AesCipher/BC 提供者）
                    val decryptedStr = decryptAES(encryptedData)
                    Log.d(TAG, "解密成功, 明文长度=${decryptedStr.length}, 前100字符: ${decryptedStr.take(100)}")
                    
                    // 用字符串替换方式重建响应 JSON，避免 JSONObject 转义问题
                    val decryptedJson = parseJsonValue(decryptedStr)
                    val resultJson = JSONObject()
                    resultJson.put("code", json.optInt("code", 0))
                    resultJson.put("msg", json.optString("msg", ""))
                    resultJson.put("data", decryptedJson)
                    
                    val newBodyString = resultJson.toString()
                    Log.d(TAG, "最终响应前200字符: ${newBodyString.take(200)}")
                    
                    val newBody = newBodyString.toResponseBody(contentType)
                    return response.newBuilder().body(newBody).build()
                }
            }
            // 非加密响应，原样返回
            val newBody = bodyString.toResponseBody(contentType)
            response.newBuilder().body(newBody).build()
        } catch (e: Exception) {
            Log.e(TAG, "解密处理异常: ${e.javaClass.simpleName}: ${e.message}", e)
            // 解密失败时仍然返回原始body
            val newBody = bodyString.toResponseBody(contentType)
            response.newBuilder().body(newBody).build()
        }
    }

    /**
     * AES/CBC 解密，与后端 AES/CBC/PKCS5Padding 对应
     * 不使用 "BC" 提供者，使用 Android 默认提供者
     */
    private fun decryptAES(encryptedBase64: String): String {
        val keyBytes = AES_KEY.toByteArray(Charsets.UTF_8)
        val ivBytes = AES_IV.toByteArray(Charsets.UTF_8)
        
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val ivSpec = IvParameterSpec(ivBytes)
        
        // 使用默认提供者，不指定 "BC"
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        
        val encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * 将解密后的字符串解析为 JSONObject 或 JSONArray
     */
    private fun parseJsonValue(jsonStr: String): Any {
        val trimmed = jsonStr.trim()
        return when {
            trimmed.startsWith("{") -> JSONObject(trimmed)
            trimmed.startsWith("[") -> JSONArray(trimmed)
            else -> trimmed
        }
    }
}
