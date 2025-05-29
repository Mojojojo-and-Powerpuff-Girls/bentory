plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.bentory_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bentory_app"
        minSdk = 24
        targetSdk = 35
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
    testOptions {
        unitTests.isReturnDefaultValues = true
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.mockito:mockito-core:5.12.0")
    testImplementation ("net.bytebuddy:byte-buddy:1.14.12")
    testImplementation ("androidx.arch.core:core-testing:2.2.0")
    testImplementation ("org.mockito:mockito-inline:4.5.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}