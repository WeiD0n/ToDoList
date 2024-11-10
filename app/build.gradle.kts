plugins {
    id("com.android.application")  // Applies the Android plugin
    id("com.google.gms.google-services") // Firebase plugin for Google services
    kotlin("android") version "1.8.22"  // Add Kotlin plugin if not already applied
}

android {
    namespace = "com.example.todolist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.todolist"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // AndroidX Libraries
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase Authentication
    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))  // Firebase BOM to manage versions
    implementation("com.google.firebase:firebase-auth:21.1.0")  // Firebase Auth

    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22")  // Ensure you're using a compatible Kotlin version

    // Testing Libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}
