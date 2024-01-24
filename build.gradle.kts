import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.9.20"
    id("org.openjfx.javafxplugin") version "0.1.0"

    id("com.github.ben-manes.versions") version "0.49.0"
    idea
}

group = "de.groovybyte.chunky"
version = "1.0"
// https://repo.lemaik.de/se/llbit/chunky-core/maven-metadata.xml
val chunkyVersion = "2.5.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://repo.lemaik.de/")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("se.llbit:chunky-core:$chunkyVersion") {
        if (chunkyVersion.endsWith("SNAPSHOT")) {
            isChanging = true
        }
    }
}

javafx {
    version = "17"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks {
    processResources {
        filesMatching("plugin.json") {
            expand(
                "version" to project.version,
                "chunkyVersion" to chunkyVersion,
            )
        }
    }

    withType<JavaCompile> {
        sourceCompatibility = "9"
        targetCompatibility = "9"
    }
    withType<KotlinJvmCompile> {
        kotlinOptions {
            javaParameters = true
            jvmTarget = "9"
            apiVersion = "1.8"
//            languageVersion = "1.8"
//            freeCompilerArgs += "-Xuse-k2"
        }
    }

    withType<Jar> {
        archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations["compileClasspath"].apply {
            files { dep ->
                when {
                    dep.name.startsWith("chunky") -> false
                    dep.name.startsWith("javafx") -> false
                    else -> true
                }
            }.forEach { file ->
                from(zipTree(file.absoluteFile))
            }
        }
    }

    withType<DependencyUpdatesTask> {
        val unstable = Regex("^.*?(?:alpha|beta|unstable|rc|ea).*\$", RegexOption.IGNORE_CASE)
        rejectVersionIf {
            candidate.version.matches(unstable)
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
