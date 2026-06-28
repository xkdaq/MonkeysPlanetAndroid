package top.monkeysxu.planet.core.network

import android.util.Base64
import top.monkeysxu.planet.BuildConfig
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AesCipher {

    private val ENCODED_KEY: String = BuildConfig.ENCODED_KEY
    private val ENCODED_IV: String = BuildConfig.ENCODED_IV
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"

    private val secretKey: SecretKeySpec by lazy {
        val keyStr = String(Base64.decode(ENCODED_KEY, Base64.DEFAULT), Charsets.UTF_8)
        SecretKeySpec(keyStr.toByteArray(Charsets.UTF_8), ALGORITHM)
    }

    private val ivSpec: IvParameterSpec by lazy {
        val ivStr = String(Base64.decode(ENCODED_IV, Base64.DEFAULT), Charsets.UTF_8)
        IvParameterSpec(ivStr.toByteArray(Charsets.UTF_8))
    }

    fun decrypt(encryptedBase64: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION, "BC")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
