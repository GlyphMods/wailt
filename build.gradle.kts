plugins {
    `java-library`
    id("idea")
    `maven-publish`
    id("net.neoforged.gradle.userdev") version "7.0.88"

    kotlin("jvm") version "1.9.23"
    // OPTIONAL Kotlin Serialization plugin
    kotlin("plugin.serialization") version "1.9.23"
}

val mod_id: String by project

version = property("mod_version") as String
group = property("mod_group_id") as String

base {
    archivesName.set(mod_id)
}

// Mojang ships Java 17 to end users in 1.18+, so your mod should target Java 17.
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

//minecraft.accessTransformers.file rootProject.file("src/main/resources/META-INF/accesstransformer.cfg")
//minecraft.accessTransformers.entry public net.minecraft.client.Minecraft textureManager # textureManager

// Default run configurations.
// These can be tweaked, removed, or duplicated as needed.
runs {
    configureEach {
        systemProperty("forge.logging.markers", "REGISTRIES")
        systemProperty("forge.logging.console.level", "debug")
        modSource(sourceSets.main.get())
    }

    create("client") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
    }

    create("server") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
        programArgument("--nogui")
    }

    create("gameTestServer") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
    }

    create("data") {
        programArguments.addAll(
            "--mod",
            mod_id,
            "--all",
            "--output",
            file("src/generated/resources/").absolutePath,
            "--existing",
            file("src/main/resources/").absolutePath
        )
    }
}

sourceSets.main.configure { resources { srcDir("src/generated/resources") } }

repositories {
    // NeoForged MDK includes mavenLocal by default
    mavenLocal()
    // REQUIRED for using Kotlin for Forge
    maven("https://thedarkcolour.github.io/KotlinForForge/") {
        name = "Kotlin for Forge"
        mavenContent { includeGroup("thedarkcolour") }
    }
}

dependencies {
    // Use the latest version of NeoForge
    implementation("net.neoforged:neoforge:${property("neo_version") as String}")

    // Must use the "-neoforge" version on NeoForge. If on regular forge, omit the "-neoforge"
    implementation("thedarkcolour:kotlinforforge-neoforge:4.10.0")
}

// This block of code expands all declared replace properties in the specified resource targets.
// A missing property will result in an error. Properties are expanded using ${} Groovy notation.
// When "copyIdeResources" is enabled, this will also run before the game launches in IDE environments.
// See https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html
tasks.withType<ProcessResources>().configureEach {
    val replaceProperties = listOf(
        "minecraft_version", "minecraft_version_range",
        "neo_version", "neo_version_range",
        "loader_version_range",
        "mod_id", "mod_name", "mod_license", "mod_version",
        "mod_authors", "mod_description", "pack_format_number"
    ).associateWith { project.properties[it] }
    inputs.properties(replaceProperties)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(replaceProperties + mapOf("project" to project))
    }
}

// Example configuration to allow publishing using the maven-publish plugin
publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
    repositories {
        maven("file://${project.projectDir}/repo")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}

kotlin {

}
