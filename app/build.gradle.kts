plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "az.zambur.namazwidget"
    compileSdk = 35
    defaultConfig {
        applicationId = "az.zambur.namazwidget"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
dependencies {
    implementation("com.batoulapps.adhan:adhan:1.2.1")
}
