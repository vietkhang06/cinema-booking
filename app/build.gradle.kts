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
    "https://cinema-booking-ful8.onrender.com/api/v1/"
)

android {
    namespace = "com.example.cinemabooking"
    compileSdk = 36

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
    implementation(libs.activityKtx)
    implementation(libs.appcompat)
    implementation(libs.gridlayout)
    implementation(libs.legacySupportV4)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.extJunit)
    androidTestImplementation(libs.espressoCore)

    // Firebase (BOM quản lý version cho tất cả firebase-* libs)
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseAuth)
    implementation(libs.firebaseFirestore)
    implementation(libs.firebaseStorage)

    // Google
    implementation(libs.playServicesAuth)
    implementation(libs.playServicesCodeScanner)

    // Facebook
    implementation(libs.facebookLogin)

    // Image loading
    implementation(libs.glide)
    annotationProcessor(libs.glideCompiler)

    // QR / Barcode
    implementation(libs.zxingCore)
    implementation(libs.zxingJavase)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofitGson)
    implementation(libs.okhttpLogging)

    // Lưu password an toàn bằng EncryptedSharedPreferences
    implementation(libs.securityCrypto)

    // Credential Manager — thay thế GoogleSignIn deprecated
    implementation(libs.credentials)
    implementation(libs.credentialsPlayServicesAuth)
    implementation(libs.googleIdentity)
}

// Dummy task to workaround Android Studio Sync error
tasks.register("prepareKotlinBuildScriptModel") {}