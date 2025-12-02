plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.cs501_fp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cs501_fp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        compose = true
    }
}

dependencies {

    /* -------------------- Jetpack Core -------------------- */
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    /* -------------------- Compose BOM (统一所有版本) -------------------- */
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    /* -------------------- Activity Compose -------------------- */
    implementation("androidx.activity:activity-compose:1.9.3")

    /* -------------------- ViewModel Compose -------------------- */
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    /* -------------------- Navigation -------------------- */
    implementation("androidx.navigation:navigation-compose:2.8.3")

    /* -------------------- Retrofit -------------------- */
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    /* -------------------- Coil -------------------- */
    implementation("io.coil-kt:coil-compose:2.2.2")

    /* -------------------- Coroutine -------------------- */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    /* -------------------- Room (Kotlin 2 Compatible) -------------------- */
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    "kapt"("androidx.room:room-compiler:$roomVersion")

    /* -------------------- Firebase -------------------- */
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    /* -------------------- Test -------------------- */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    /* -------------------- Debug -------------------- */
    debugImplementation("androidx.compose.ui:ui-tooling")
}