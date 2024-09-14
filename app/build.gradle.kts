plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.postventaandroid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.postventaandroid"
        minSdk = 26 //SDK minimo fué el 21, pero la dependencia para integrar firma al PDF necesita que sea minimo de 26(Android Oreo)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

    //Fue añadido el 12-06-2024 para evitar:
    // error 3 files found with path 'META-INF/DEPENDENCIES'.
    //Adding a packaging block may help
    // de la dependencia para integrar firma en PDF
    packaging{
        resources.excludes.add("META-INF/DEPENDENCIES")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Firebase implementacion notificaciones mejoradas
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation ("com.google.firebase:firebase-core:21.1.1")

    //Libreria HTTP volley
    implementation("com.android.volley:volley:1.2.1")

    //para  recibir notificaciones en segundo plano
    // work
    implementation ("androidx.work:work-runtime-ktx:2.9.0")

    //Para PDF
    implementation ("com.dmitryborodin:pdfview-android:1.1.0")

    //Para charts open source
    implementation ("com.github.philJay:MPAndroidChart:v3.1.0")

    //Añadido 22-05-24 -  para powerBi
    /*implementation ("com.microsoft.identity.client:msal:5.3.1")
    implementation ("com.android.volley:volley:1.2.1")*/
    //-----------

    //Para integrar firma al PDF
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation ("org.apache.pdfbox:pdfbox:3.0.2")
    implementation("com.itextpdf:itextg:5.5.10")

    //SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //Para coroutines kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

//    Si da error cambiar el nombre 'HARDMACHINE' al nombre de usuario de la pc en la cual se ejecuta este codigo
//    PARA CONEXION SQL server
    implementation(files("C:/Users/chivo/Desktop/promt/PostVentaAndroid/app/libs/jtds-1.3.1.jar"))
}