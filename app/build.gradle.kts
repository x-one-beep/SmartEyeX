import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

val secretsProps = Properties().apply {
    val f = rootProject.file("app/secrets.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.smarteyex.core"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smarteyex.core"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GROQ_KEY",
            "\"${secretsProps.getProperty("groq") ?: ""}\""
        )
        buildConfigField(
            "String",
            "WEATHER_KEY",
            "\"${secretsProps.getProperty("weather") ?: ""}\""
        )
    }

    buildFeatures {
        viewBinding = true
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
            // DEBUG key dulu, signing nanti
        }
    }
}

val cameraxVersion = "1.3.2"

dependencies {
    // CameraX
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Lifecycle + Coroutines
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Preference & WorkManager
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    
    // Material + Lottie
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.airbnb.android:lottie:5.2.0")
}
