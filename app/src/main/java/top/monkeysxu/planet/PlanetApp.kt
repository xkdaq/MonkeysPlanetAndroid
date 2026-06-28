package top.monkeysxu.planet

import android.app.Application
import coil.Coil
import coil.ImageLoader
import okhttp3.OkHttpClient
import okhttp3.Cache
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.security.Security

class PlanetApp : Application() {

    companion object {
        lateinit var instance: PlanetApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 注册 BouncyCastle Provider 以支持 AES/CBC/PKCS7Padding
        Security.addProvider(BouncyCastleProvider())

        // 初始化 Coil 图片加载器
        val imageLoader = ImageLoader.Builder(this)
            .crossfade(true)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(Cache(File(cacheDir, "image_cache"), 50 * 1024 * 1024))
                    .build()
            }
            .build()
        Coil.setImageLoader(imageLoader)
    }
}
