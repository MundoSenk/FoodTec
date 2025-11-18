plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)

}

android {
    namespace = "host.senk.foodtec"
    compileSdk = 36

    defaultConfig {
        applicationId = "host.senk.foodtec"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // El bloque 'testOptions' fue removido ya que usaremos la exclusión por terminal.

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Retrofit: El "cartero" que hace la llamada a la API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson Converter: El "traductor" que convierte el JSON a objetos de Kotlin
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    /// TabLayout del inicio Sesion
    implementation("com.google.android.material:material:1.12.0")
    ///// GLIDE para las imagenes en la nube
    implementation("com.github.bumptech.glide:glide:4.16.0")


    implementation("de.hdodenhof:circleimageview:3.1.0")

    ksp("com.github.bumptech.glide:ksp:4.16.0")


    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")


}