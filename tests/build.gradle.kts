plugins {
    kotlin("jvm")
}

group = "com.github.enteraname74.exposedflows"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(libs.bundles.exposed)
    implementation(libs.sqlite.jdbc)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core"))
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}