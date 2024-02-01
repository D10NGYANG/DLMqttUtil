plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = "com.github.D10NGYANG"
version = "0.0.5"

android {
    namespace = "com.d10ng.mqtt"
    compileSdk = android_compile_sdk

    defaultConfig {
        minSdk = android_min_sdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlin {
        jvmToolchain(8)
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    // MQTT
    api("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    api("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_coroutines_ver")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlin_coroutines_ver")
}

val bds100MavenUsername: String by project
val bds100MavenPassword: String by project

afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication::class) {
                artifactId = "DLMqttUtil"
                from(components.getByName("release"))
            }
        }
        repositories {
            maven {
                url = uri("/Users/d10ng/project/kotlin/maven-repo/repository")
            }
            maven {
                credentials {
                    username = bds100MavenUsername
                    password = bds100MavenPassword
                }
                setUrl("https://nexus.bds100.com/repository/maven-releases/")
            }
        }
    }
}