package io.github.glyphmods.wailt

import net.neoforged.neoforge.common.ModConfigSpec

object Config {
    private val builder = ModConfigSpec.Builder()

    val metadataUrl =
        builder.comment("The URL to download track metadata from. This should generally be left at the default.")
            .define(
                "metadata.url",
                "https://raw.githubusercontent.com/GlyphMods/wailt/1.20.2-neoforge/src/main/resources/songs/"
            )
    val forceEmbeddedMetadata =
        builder.comment("Force the mod to use track metadata embedded in the JAR. Only useful for development.")
            .define("metadata.force-embedded", false)

    val toastDuration = builder.comment("How long to show the toast on screen, in seconds")
        .define("toast.duration", 5.0)


    val spec = builder.build()
}