plugins {
    alias(libs.plugins.android.application)

    // ✅ FIREBASE PLUGIN
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.transporttrackingsystem"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.transporttrackingsystem"
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

    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res-activities",
                "src/main/res-fragments",
                "src/main/res-items",
                "src/main/res"
            )
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // 🗺️ GOOGLE MAPS
    implementation(libs.play.services.maps)

    // 🔥 FIREBASE BOOT
    implementation(platform(libs.firebase.bom))

    // 🔥 FIRESTORE (IMPORTANT FOR BUS TRACKING)
    implementation(libs.firebase.firestore)

    // 🔥 OPTIONAL (analytics)
    implementation(libs.firebase.analytics)

    // 🔥 AUTHENTICATION
    implementation(libs.firebase.auth)

    // 📍 LOCATION SERVICES
    implementation(libs.play.services.location)

    // 📧 EMAIL SERVICES (JavaMail API)
    implementation("com.sun.mail:android-mail:1.6.2")
    implementation("com.sun.mail:android-activation:1.6.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}