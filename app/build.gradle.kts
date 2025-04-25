plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "net.senekal.openchrono"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.senekal.openchrono"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        android.buildFeatures.buildConfig = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            postprocessing {
                isRemoveUnusedCode = false
                isObfuscate = false
                isOptimizeCode = false
                isMinifyEnabled = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("assets", "assets")
        }
    }

    // Define the generateCsv task
    tasks.register<Exec>("generateCsv") {
        println("Project directory: $projectDir")
        commandLine("python", "${projectDir}/assets/file_info.py")
    }

    // Specify that assembling the APK depends on the generateCsv task
    tasks.named("assemble") {
        dependsOn("generateCsv")
    }

    tasks.named("preBuild") {
        dependsOn("generateCsv")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.rxandroidble)
    implementation(libs.room.runtime)

    annotationProcessor(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}