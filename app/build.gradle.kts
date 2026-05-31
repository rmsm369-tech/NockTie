import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}
android {
    namespace = "com.nyxtesla.talk2u"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.nyxtesla.talk2u"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.4" }

    val localProps = run {
        val file = rootProject.file("local.properties")
        val props = Properties()
        if (file.exists()) {
            try { file.inputStream().use { props.load(it) } } catch (_: Exception) {}
        }
        props
    }

    // No local AI provider keys are exposed here; AI is handled by the external web app or secure worker.
    // Support two worker URLs: primary and secondary. Fall back to the legacy WORKER_URL if PRIMARY not provided.
    val workerUrl = localProps.getProperty("WORKER_URL") ?: project.findProperty("WORKER_URL")?.toString() ?: ""
    val workerUrlPrimary = localProps.getProperty("WORKER_URL_PRIMARY") ?: project.findProperty("WORKER_URL_PRIMARY")?.toString() ?: localProps.getProperty("WORKER_URL") ?: project.findProperty("WORKER_URL")?.toString() ?: ""
    val workerUrlSecondary = localProps.getProperty("WORKER_URL_SECONDARY") ?: project.findProperty("WORKER_URL_SECONDARY")?.toString() ?: ""

    // Paste the signingConfigs block right here:
    signingConfigs {
        create("release") {
            storeFile = rootProject.file("nocktie-release.jks")
            storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD") ?: ""
            keyAlias = "nocktie-key"
            keyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            
            // ADD THIS LINE so it actually uses the keystore you just linked:
            signingConfig = signingConfigs.getByName("release") 
            
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "WORKER_URL", "\"$workerUrl\"")
            buildConfigField("String", "WORKER_URL_PRIMARY", "\"$workerUrlPrimary\"")
            buildConfigField("String", "WORKER_URL_SECONDARY", "\"$workerUrlSecondary\"")
        }
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    kapt("androidx.room:room-compiler:2.6.1")
}

