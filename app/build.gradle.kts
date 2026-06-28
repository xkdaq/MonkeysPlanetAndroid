import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

/**
 * 从项目根目录读取 secrets.properties 配置文件
 * 该文件已在 .gitignore 中忽略，不上传到 Git 仓库
 */
val secretsFile = rootProject.file("secrets.properties")
val secrets = java.util.Properties().apply {
    if (secretsFile.exists()) {
        secretsFile.inputStream().use { load(it) }
    } else {
        // 如果文件不存在，使用默认空值，避免构建失败
        logger.warn("⚠️ secrets.properties 不存在！请复制 secrets.properties.example 并填写真实值。")
    }
}

fun getSecret(key: String, default: String = ""): String {
    return secrets.getProperty(key, default)
}

// AES 密钥的 Base64 编码
fun base64Encode(input: String): String {
    return Base64.getEncoder().encodeToString(input.toByteArray(Charsets.UTF_8)).trim()
}

android {
    namespace = "top.monkeysxu.planet"
    compileSdk = 36

    defaultConfig {
        applicationId = "top.monkeysxu.planet"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ========== 从 secrets.properties 读取敏感配置 ==========
        // AesCipher.kt - Base64 编码的 AES 密钥和 IV
        buildConfigField("String", "ENCODED_KEY", "\"${base64Encode(getSecret("AES_KEY"))}\"")
        buildConfigField("String", "ENCODED_IV", "\"${base64Encode(getSecret("AES_IV"))}\"")
        // ExamDecryptInterceptor.kt - 明文 AES 密钥和 IV
        buildConfigField("String", "AES_KEY", "\"${getSecret("AES_KEY")}\"")
        buildConfigField("String", "AES_IV", "\"${getSecret("AES_IV")}\"")
        // SignInterceptor.kt - 签名密钥
        buildConfigField("String", "SIGN_KEY", "\"${getSecret("SIGN_KEY")}\"")
        // RetrofitClient.kt - API 基础地址
        buildConfigField("String", "BASE_URL", "\"${getSecret("BASE_URL")}\"")
        // API 版本号
        buildConfigField("String", "ARTICLE_API_VERSION", "\"${getSecret("ARTICLE_API_VERSION", "10")}\"")
        buildConfigField("String", "MATERIAL_API_VERSION", "\"${getSecret("MATERIAL_API_VERSION", "8")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.datastore)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Image
    implementation(libs.coil)

    // UI
    implementation(libs.banner)
    implementation(libs.brvah)

    // Crypto
    implementation(libs.bouncycastle)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
}
