object Dependencies {

    const val detektFormatting = "io.gitlab.arturbosch.detekt:detekt-formatting:${Version.detekt}"
    const val mockk = "io.mockk:mockk:1.10.0"
    const val kotlinxCoroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Version.kotlinxCoroutines}"
    const val kotlinxCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.kotlinxCoroutines}"
    const val testCommon = "test-common"
    const val testAnnotationsCommon = "test-annotations-common"
    const val testJunit = "test-junit"
    const val androidxLifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Version.androidxLifecycle}"
    const val androidxLifecycleLiveData = "androidx.lifecycle:lifecycle-livedata-core-ktx:${Version.androidxLifecycle}"
    const val androidxCoreTesting = "androidx.arch.core:core-testing:2.1.0"

    object Plugin {
        const val detekt = "io.gitlab.arturbosch.detekt"
        const val multiplatform = "multiplatform"
        const val mavenPublish = "maven-publish"
        const val androidLibrary = "com.android.library"
    }

    object Version {
        const val kotlin = "1.4.21"
        const val kotlinxCoroutines = "1.4.2"
        const val detekt = "1.15.0"
        const val androidxLifecycle = "2.2.0"
    }
}
