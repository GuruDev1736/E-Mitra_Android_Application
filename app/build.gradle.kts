plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.guruprasad.tutionnotesaplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.guruprasad.tutionnotesaplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
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
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    viewBinding{
        enable = true
    }
    buildFeatures {
        viewBinding = true
    }
}


dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth-ktx:22.1.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.3")
    implementation("com.google.firebase:firebase-database-ktx:20.2.2")
    implementation("com.google.firebase:firebase-storage-ktx:20.2.1")
    implementation("com.google.firebase:firebase-auth:22.2.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-crashlytics:18.5.0")
    implementation("com.google.firebase:firebase-analytics:21.4.0")
    implementation("com.google.firebase:firebase-perf:20.5.0")
    implementation("com.google.firebase:firebase-messaging:23.3.0")
    implementation("com.google.firebase:firebase-inappmessaging-display:20.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7") {
        version { require("1.8.20") }
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") {
        version { require("1.8.20") }
    }

    implementation("com.airbnb.android:lottie:6.1.0")
    implementation ("com.karumi:dexter:6.2.3")
    implementation ("com.github.GrenderG:Toasty:1.5.2")
    implementation ("com.firebaseui:firebase-ui-database:8.0.2")

    // pdf viewer
    implementation ("com.github.afreakyelf:Pdf-Viewer:v1.0.7")

    //retrofit

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")


    implementation(platform("com.google.firebase:firebase-bom:32.4.0"))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.4.0"))

    // Add the dependency for the Performance Monitoring library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-perf")


    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")




}