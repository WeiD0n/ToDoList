// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1") // Latest Android Gradle Plugin version
        classpath("com.google.gms:google-services:4.3.15") // Latest version of Google services

    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
