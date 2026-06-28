package top.monkeysxu.planet.core.network

import okhttp3.OkHttpClient
import top.monkeysxu.planet.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import top.monkeysxu.planet.core.storage.TokenManager

object RetrofitClient {

    private val BASE_URL: String = BuildConfig.BASE_URL

    // 用于 /mp/ 路径的 Retrofit 实例（需要 Token + 签名 + 加密接口解密）
    fun createMpRetrofit(tokenManager: TokenManager): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(SignInterceptor())
            .addInterceptor(TokenInterceptor(tokenManager))
            .addInterceptor(ResponseLogInterceptor("API_MP"))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .addInterceptor(ExamDecryptInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 用于 /api/article/ 路径的 Retrofit 实例（AES 解密 + x-version: 10）
    fun createArticleRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AesVersionInterceptor(BuildConfig.ARTICLE_API_VERSION))
            .addInterceptor(AesDecryptInterceptor())
            .addInterceptor(ResponseLogInterceptor("API_ARTICLE"))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 用于 /api/material/ 路径的 Retrofit 实例（AES 解密 + x-version: 8）
    fun createMaterialRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AesVersionInterceptor(BuildConfig.MATERIAL_API_VERSION))
            .addInterceptor(AesDecryptInterceptor())
            .addInterceptor(ResponseLogInterceptor("API_MATERIAL"))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
