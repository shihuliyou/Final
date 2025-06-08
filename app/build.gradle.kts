plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.afinal"
    compileSdk = 35

    defaultConfig {
        applicationId        = "com.example.afinal"
        minSdk               = 21
        targetSdk            = 35
        versionCode          = 1
        versionName          = "1.0"
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime.android)
    implementation(libs.room.ktx)               // <-- 新增：Room KTX 扩展
    annotationProcessor(libs.room.compiler)
    implementation(libs.okhttp)
    implementation(libs.swiperefreshlayout)
    implementation(libs.material.v180)
    implementation(libs.jsoup)

    implementation(libs.lifecycle.runtime.ktx)  // <-- 新增：Lifecycle Runtime KTX

    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
