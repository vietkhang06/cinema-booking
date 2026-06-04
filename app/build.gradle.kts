import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

val baseUrl: String = localProperties.getProperty(
    "BASE_URL",
    "http://10.0.2.2:8080/api/v1/"
)

android {
    namespace = "com.example.cinemabooking"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.cinemabooking"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

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
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.gridlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase (BOM quản lý version cho tất cả firebase-* libs)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Google
    implementation(libs.play.services.auth)
    implementation(libs.play.services.code.scanner)

    // Facebook
    implementation(libs.facebook.login)

    // Image loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // QR / Barcode
    implementation(libs.zxing.core)
    implementation(libs.zxing.javase)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Lưu password an toàn bằng EncryptedSharedPreferences
    implementation(libs.security.crypto)

    // Credential Manager — thay thế GoogleSignIn deprecated
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.google.identity)
}