plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.indusave"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.indusave"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Versiones forzadas para compatibilidad con SDK 34
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Solución al error: Usamos la versión 1.8.0 que sí es compatible con SDK 34
    implementation("androidx.activity:activity:1.8.0")

    // Librería para CouchDB (Sincronización en la nube)
    implementation("com.android.volley:volley:1.2.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}