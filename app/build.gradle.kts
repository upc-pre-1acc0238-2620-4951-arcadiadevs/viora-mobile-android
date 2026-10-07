import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

// Per-developer overrides live in local.properties (never committed), e.g.:
//   viora.apiBaseUrl=http://192.168.1.50:8080/api/v1/   (backend on another machine)
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

// Public Mapbox token (pk.…). It is not a secret, but keep it out of git: set it in local.properties.
val defaultMapboxToken = "pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3cifQ.rJcFIG2TW4iGNnFiOecFGQ"
val mapboxPublicToken: String = localProperties.getProperty("viora.mapboxPublicToken", defaultMapboxToken)

// Android emulator alias for the host machine's localhost (backend running locally).
val defaultDebugApiBaseUrl = "http://10.0.2.2:8080/api/v1/"
val defaultReleaseApiBaseUrl = "https://viora-platform.onrender.com/api/v1/"

room3 {
    // Exported schemas are committed so migrations can be reviewed and tested.
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "pe.edu.upc.viora"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pe.edu.upc.viora"
        minSdk = 26
        targetSdk = 37
        versionCode = 6
        versionName = "0.16.0"

        // The Maps SDK reads this string resource at startup.
        resValue("string", "mapbox_access_token", mapboxPublicToken)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val url = localProperties.getProperty("viora.apiBaseUrl", defaultDebugApiBaseUrl)
            buildConfigField("String", "API_BASE_URL", "\"$url\"")
        }
        release {
            val url = localProperties.getProperty("viora.apiBaseUrl", defaultReleaseApiBaseUrl)
            buildConfigField("String", "API_BASE_URL", "\"$url\"")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Background work (offline settlement sync)
    implementation(libs.androidx.work.runtime.ktx)

    // Network
    implementation(libs.retrofit)
    implementation(libs.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Persistence
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Maps
    implementation(libs.mapbox.maps)
    implementation(libs.mapbox.maps.compose)
    implementation(libs.play.services.location)

    // Images
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Kotlin
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)

    // Instrumented tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
