plugins {
    id(Dependencies.Plugin.androidLibrary)
    kotlin(Dependencies.Plugin.multiplatform) version Dependencies.Version.kotlin
    id(Dependencies.Plugin.detekt) version Dependencies.Version.detekt
    id(Dependencies.Plugin.mavenPublish)
}

group = "org.avmteam"
version = "0.9.25"

repositories {
    google()
    mavenCentral()
}

kotlin {
    android {
        publishLibraryVariants("debug", "release")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Dependencies.kotlinxCoroutinesCore)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(Dependencies.androidxLifecycleExtensions)
                implementation(Dependencies.androidxLifecycleLiveData)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin(Dependencies.testJunit))
                implementation(Dependencies.kotlinxCoroutinesTest)
                implementation(Dependencies.mockk)
                implementation(kotlin(Dependencies.testCommon))
                implementation(kotlin(Dependencies.testAnnotationsCommon))
                implementation(Dependencies.androidxCoreTesting)
            }
        }
    }

    publishing {
        repositories {
            maven {
                url = uri("")
                credentials(HttpHeaderCredentials::class) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN")
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }

        afterEvaluate {
            configure<PublishingExtension> {
                publications.all {
                    val mavenPublication = this as? MavenPublication
                    mavenPublication?.artifactId = when (name) {
                        "kotlinMultiplatform" -> "udf-multiplatform"
                        else -> "udf-$name"
                    }
                }
            }
        }

    }
}

android {
    compileSdkVersion(29)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(29)
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    input = files("$rootDir/src", "$rootDir/buildSrc")
    toolVersion = Dependencies.Version.detekt
    config = files("$rootDir/config/detekt/detekt.yml")

    reports {
        html.enabled = true
        txt.enabled = true
    }
}

dependencies {
    detektPlugins(Dependencies.detektFormatting)
}
