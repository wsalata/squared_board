plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Podpis: w CI ze zmiennych środowiskowych, lokalnie z ~/.gradle/gradle.properties.
// Gdy nie ma żadnej z nich, release buduje się niepodpisany — tak jak dotąd.
val releaseStoreFile = providers.environmentVariable("KEYSTORE_PATH")
    .orElse(providers.gradleProperty("SB_STORE_FILE")).orNull
val releaseStorePassword = providers.environmentVariable("KEYSTORE_PASSWORD")
    .orElse(providers.gradleProperty("SB_STORE_PASSWORD")).orNull
val releaseKeyAlias = providers.environmentVariable("KEY_ALIAS")
    .orElse(providers.gradleProperty("SB_KEY_ALIAS")).orNull
val releaseKeyPassword = providers.environmentVariable("KEY_PASSWORD")
    .orElse(providers.gradleProperty("SB_KEY_PASSWORD")).orNull

// Każdy plik wysłany do sklepu musi mieć wyższy versionCode niż poprzedni.
val ciVersionCode = providers.environmentVariable("VERSION_CODE").orNull?.toInt()

android {
    namespace = "eu.tudek.squared_board"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "eu.tudek.squared_board"
        minSdk = 28
        targetSdk = 37
        versionCode = ciVersionCode ?: 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            // Robolectric renders the Compose screens on the JVM, so no device is needed.
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
}
