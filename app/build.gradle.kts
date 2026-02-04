import java.util.Properties

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

val secretsProps = Properties().apply {
    val f = rootProject.file("core/secrets.properties")
    if (f.exists()) {
        f.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.smarteyex.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Opsional: jika mau pakai API key di core
        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${secretsProps["GROQ_API_KEY"] ?: "DUMMY_API_KEY"}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
        }
    }
}

val cameraxVersion = "1.3.2"
val lifecycleVersion = "2.7.0"
val roomVersion = "2.6.1"
val coroutinesVersion = "1.7.3"
val activityVersion = "1.9.0"

dependencies {
    // Lifecycle / Service
    implementation("androidx.lifecycle:lifecycle-service:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Activity KTX (untuk UI hooks jika core akses)
    implementation("androidx.activity:activity-ktx:$activityVersion")

    // CameraX
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Room Database
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // WorkManager & Preferences
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.preference:preference-ktx:1.2.0")

    // Material Design + Lottie
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.airbnb.android:lottie:5.2.0")

    // Core Android KTX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // JSON & HTTP
    implementation("org.json:json:20230227")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Kotlin Standard Library
    implementation(kotlin("stdlib"))
}