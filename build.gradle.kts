import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.openjfx.javafxplugin") version "0.0.13"

    id("com.github.ben-manes.versions") version "0.42.0"
    idea
}

group = "de.groovybyte.chunky"
version = "1.0"
// https://repo.lemaik.de/se/llbit/chunky-core/maven-metadata.xml
val chunkyVersion = "2.5.0-20220304.113004-103"

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://repo.lemaik.de/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("se.llbit:chunky-core:$chunkyVersion") {
        isChanging = true
    }
}

javafx {
    version = "18.0.2"
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
            apiVersion = "1.7"
            languageVersion = "1.7"
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
        val regex = Regex("^[0-9,.v-]+(-r)?\$")
        fun isNonStable(version: String): Boolean {
            val stableKeyword = listOf("RELEASE", "FINAL", "GA")
                .any { keyword -> version.toUpperCase().contains(keyword) }
            return !stableKeyword && !regex.matches(version)
        }

        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
