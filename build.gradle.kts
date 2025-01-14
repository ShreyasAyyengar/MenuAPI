import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm") version "2.1.20-Beta1"
    id("com.gradleup.shadow") version "latest.release"
    id("io.papermc.paperweight.userdev") version "latest.release"
    id("maven-publish")
}

group = "dev.shreyasayyengar"
version = "1.0.0"

val targetJavaVersion = 21

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.flyte.gg/releases")
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("reflect"))

    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")

    implementation("gg.flyte:twilight:1.1.17")
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar.get()) {
                classifier = null // Publish the shadow JAR without a classifier
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks {
    build { dependsOn(shadowJar) }
    assemble { dependsOn(reobfJar) }

    shadowJar {
        archiveClassifier.set("shadow") // Sets the classifier for the shadow JAR

        minimize {
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
        }
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(targetJavaVersion)
    }

    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    javadoc { options.encoding = Charsets.UTF_8.name() }
    processResources { filteringCharset = Charsets.UTF_8.name() }
}

kotlin {
    compilerOptions { javaParameters = true } // Lamp CMD Framework
}

application { mainClass.set("MenuPluginTestKt") }