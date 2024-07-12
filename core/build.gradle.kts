plugins {
    kotlin("jvm")
}

group = "com.github.enteraname74.exposedflows"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(libs.exposed.core)
    implementation(libs.kotlinx.coroutines.core)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveFileName.set("exposed-flows-core-$version.jar")
}

kotlin {
    jvmToolchain(17)
}