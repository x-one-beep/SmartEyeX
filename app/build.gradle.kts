plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    namespace = "com.smarteyex.fullcore"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smarteyex.fullcore"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.9"

        buildConfigField(
            "String",
            "API_KEY",
            "\"${System.getenv("OPENAI_API_KEY") ?: ""}\""
        )
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Kotlin + Coroutines
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // ML Kit Face Detection
    implementation("com.google.mlkit:face-detection:16.1.6")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
}

// Optional: logging / debug utilities
dependencies {
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
    testImplementation("junit:junit:4.13.2")
}