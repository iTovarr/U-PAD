import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.upad"
    // Compose 1.12 exige compilar contra el SDK 37. targetSdk sigue en 36, así que
    // el comportamiento de la app en los dispositivos no cambia.
    compileSdk {
        version = release(37)
    }

    val localProperties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream ->
            localProperties.load(stream)
        }
    }
    val groqApiKey = localProperties.getProperty("GROQ_API_KEY") ?: ""
    val googleClientId = localProperties.getProperty("GOOGLE_CLIENT_ID") ?: ""
    val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
    // URL de la Cloud Function `aiChat`. Si está definida, la app usa el proxy y
    // GROQ_API_KEY puede (y debe) dejarse vacía para que no viaje en el APK.
    val aiProxyUrl = localProperties.getProperty("AI_PROXY_URL") ?: ""

    // Firma de release: solo se configura si existe keystore.properties en la raíz
    // (ver keystore.properties.example). Sin él, assembleRelease genera un APK sin firmar.
    val keystoreProperties = Properties()
    val keystorePropertiesFile = project.rootProject.file("keystore.properties")
    val tieneFirmaRelease = keystorePropertiesFile.exists()
    if (tieneFirmaRelease) {
        keystorePropertiesFile.inputStream().use { stream ->
            keystoreProperties.load(stream)
        }
    }

    signingConfigs {
        if (tieneFirmaRelease) {
            create("release") {
                storeFile = project.rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.upad"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
        buildConfigField("String", "AI_PROXY_URL", "\"$aiProxyUrl\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            // R8 reduce y ofusca el código; shrinkResources elimina recursos sin uso.
            isMinifyEnabled = true
            isShrinkResources = true
            if (tieneFirmaRelease) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        // Robolectric necesita los recursos (textos, fuentes e imágenes) en los tests de la JVM.
        unitTests.isIncludeAndroidResources = true
        unitTests.all { test ->
            // Carpeta donde los tests de capturas guardan las imágenes de las pantallas.
            test.systemProperty("upad.capturas", rootProject.file("docs/capturas").absolutePath)
        }
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.play.services)

    // Inicio de sesión con Google (Credential Manager) y huella digital
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.androidx.biometric)

    // Red: Retrofit para ARASAAC, OkHttp para la IA y Coil para los pictogramas
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.coil.compose)

    // Mapas y ubicación
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
