import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.COMPILE

plugins {
    kotlin("jvm") version "2.1.21"
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
}

group = property("maven_group")!!
version = property("mod_version")!!

val minecraft_version: String by project
val loader_version: String by project
val yarn_mappings: String by project
val fabric_kotlin_version: String by project
val fabric_api_version: String by project

repositories {
    mavenCentral()
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://jitpack.io")
    maven("https://jcenter.bintray.com")
    maven("https://repo1.maven.org/maven2/")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")

}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "loader_version" to loader_version,
                "minecraft_version" to minecraft_version,
                "fabric_kotlin_version" to fabric_kotlin_version
            )
        }
    }

    jar {
        from("LICENSE")
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
            freeCompilerArgs.set(listOf("-Xnon-local-break-continue"))
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }
    }
}

java {
    withSourcesJar()
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java", "src/main/kotlin"))
    }
}